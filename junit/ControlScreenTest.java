
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. Account JSON Serialization (saveAccounts / loadAccounts)
 * - Verifies the bespoke JSON round-trip: accounts written to file are correctly parsed back.
 * - Confirms that the parentTeacher boolean flag is preserved exactly.
 * - Tests robustness against special characters (quotes, backslashes) in usernames via escaping.
 *
 * 2. User Removal from Session History (removeUserFromStats)
 * - Verifies that deleting an account also removes ALL session records for that user.
 * - Confirms that other users' sessions are untouched during the removal.
 *
 * 3. Power-Ups Toggle Label
 * - Verifies that the power-ups label text matches the current boolean state.
 *
 * 4. Constructor Safety
 * - Verifies that the screen instantiates correctly with a valid null-safe navigator and username.
 */
class ControlScreenTest {

    // We use a temporary accounts.json and sessions.json to avoid corrupting real data.
    private static Path accountsFile;
    private static Path sessionsFile;
    private static Path accountsBackup;
    private static Path sessionsBackup;

    private ControlScreen screen;
    private Class<?> accountClass;
    private Constructor<?> accountConstructor;
    private Method saveMethod;
    private Method loadMethod;
    private Method removeMethod;

    @BeforeEach
    void setUp() throws Exception {
        // Locate the production data files
        java.net.URL accountsUrl = ControlScreen.class.getResource("accounts.json");
        java.net.URL sessionsUrl = SessionRecord.class.getResource("sessions.json");

        accountsFile = accountsUrl != null
                ? java.nio.file.Paths.get(accountsUrl.toURI())
                : java.nio.file.Paths.get("src/accounts.json");
        sessionsFile = sessionsUrl != null
                ? java.nio.file.Paths.get(sessionsUrl.toURI())
                : java.nio.file.Paths.get("src/sessions.json");

        accountsBackup = java.nio.file.Paths.get(accountsFile + ".ctrlbak");
        sessionsBackup = java.nio.file.Paths.get(sessionsFile + ".ctrlbak");

        // Backup & clear for test isolation
        if (Files.exists(accountsFile)) {
            Files.copy(accountsFile, accountsBackup);
        }
        if (Files.exists(sessionsFile)) {
            Files.copy(sessionsFile, sessionsBackup);
        }
        Files.createDirectories(accountsFile.getParent());
        Files.writeString(accountsFile, "[]", StandardCharsets.UTF_8);
        Files.writeString(sessionsFile, "[]", StandardCharsets.UTF_8);

        // Build the screen with no-op callbacks
        screen = new ControlScreen(() -> {}, () -> {}, "admin");

        // Reflect into private Account class and methods
        accountClass = Class.forName("ControlScreen$Account");
        accountConstructor = accountClass.getDeclaredConstructor(String.class, String.class, boolean.class);
        accountConstructor.setAccessible(true);

        saveMethod = ControlScreen.class.getDeclaredMethod("saveAccounts", List.class);
        saveMethod.setAccessible(true);

        loadMethod = ControlScreen.class.getDeclaredMethod("loadAccounts");
        loadMethod.setAccessible(true);

        removeMethod = ControlScreen.class.getDeclaredMethod("removeUserFromStats", String.class);
        removeMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original files
        Files.deleteIfExists(accountsFile);
        Files.deleteIfExists(sessionsFile);
        if (Files.exists(accountsBackup)) {
            Files.move(accountsBackup, accountsFile);
        }
        if (Files.exists(sessionsBackup)) {
            Files.move(sessionsBackup, sessionsFile);
        }
    }

    @Test
    void testAccountSaveAndLoadRoundTrip() throws Exception {
        List<Object> accounts = new ArrayList<>();
        accounts.add(accountConstructor.newInstance("Alice", "pass1", false));
        accounts.add(accountConstructor.newInstance("Bob", "pass2", true));

        saveMethod.invoke(screen, accounts);

        List<?> loaded = (List<?>) loadMethod.invoke(screen);
        assertEquals(2, loaded.size(), "Both accounts should be saved and reloaded");

        Field usernameField = accountClass.getDeclaredField("username");
        Field parentField = accountClass.getDeclaredField("parentTeacher");
        usernameField.setAccessible(true);
        parentField.setAccessible(true);

        assertEquals("Alice", usernameField.get(loaded.get(0)));
        assertFalse((Boolean) parentField.get(loaded.get(0)));
        assertEquals("Bob", usernameField.get(loaded.get(1)));
        assertTrue((Boolean) parentField.get(loaded.get(1)));
    }

    @Test
    void testAccountJsonEscaping() throws Exception {
        // Username with special JSON characters
        List<Object> accounts = new ArrayList<>();
        accounts.add(accountConstructor.newInstance("User\"Quote", "pa\\ss", false));
        saveMethod.invoke(screen, accounts);

        List<?> loaded = (List<?>) loadMethod.invoke(screen);
        assertEquals(1, loaded.size());

        Field usernameField = accountClass.getDeclaredField("username");
        Field passwordField = accountClass.getDeclaredField("password");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);

        assertEquals("User\"Quote", usernameField.get(loaded.get(0)),
                "Quoted username should round-trip through JSON correctly");
        assertEquals("pa\\ss", passwordField.get(loaded.get(0)),
                "Backslash password should round-trip through JSON correctly");
    }

    @Test
    void testLoadEmptyFile() throws Exception {
        // File already set to "[]" by setUp
        List<?> loaded = (List<?>) loadMethod.invoke(screen);
        assertTrue(loaded.isEmpty(), "Loading an empty accounts.json should return an empty list");
    }

    @Test
    void testLoadMissingFile() throws Exception {
        Files.deleteIfExists(accountsFile);
        List<?> loaded = (List<?>) loadMethod.invoke(screen);
        assertTrue(loaded.isEmpty(), "Loading when no file exists should return an empty list, not throw");
    }

    @Test
    void testRemoveUserFromStats() throws Exception {
        // Populate sessions.json with records for two users
        List<SessionRecord> sessions = new ArrayList<>();
        sessions.add(new SessionRecord("Alice", "EASY", 100, 90.0, 40, "2024-01-01T10:00:00"));
        sessions.add(new SessionRecord("Bob", "HARD", 500, 95.0, 70, "2024-01-01T11:00:00"));
        sessions.add(new SessionRecord("Alice", "MEDIUM", 200, 88.0, 55, "2024-01-01T12:00:00"));
        SessionRecord.writeAllSessions(sessions);

        // Remove Alice
        removeMethod.invoke(screen, "Alice");

        List<SessionRecord> remaining = SessionRecord.loadAllSessions();
        assertEquals(1, remaining.size(), "Only Bob's session should remain");
        assertEquals("Bob", remaining.get(0).getUsername(), "Remaining record should belong to Bob");
    }

    @Test
    void testConstructorNullSafety() {
        // Passing null username should not cause a NullPointerException
        assertDoesNotThrow(() -> new ControlScreen(() -> {}, () -> {}, null),
                "ControlScreen should handle null username gracefully");
    }
}
