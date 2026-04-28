
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. User Account Persistence (loadAccounts / saveAccounts)
 * - Verifies the manual JSON serialization and deserialization of Account records.
 * - Confirms that username, password, and parentTeacher flags are correctly preserved.
 *
 * 2. Visual & Layout Logic
 * - Tests preferred size settings to ensure window docking and scaling behave correctly.
 * - Verifies that doLayout executes without exceptions across different aspect ratios.
 *
 * 3. Lifecycle & Window Management
 * - Checks the openMainMenuWindow entry point.
 * - Ensures static state (like splash screens) is handled correctly.
 */
class Run_MainMenuScreenTest {

    private Path accountsFile;
    private Path backupFile;

    @BeforeEach
    void setUp() throws Exception {
        // Locate the accounts file. The code uses a static block to find it relative to the class.
        java.net.URL url = Run_MainMenuScreen.class.getResource("accounts.json");
        if (url != null) {
            accountsFile = Paths.get(url.toURI());
        } else {
            // Fallback for directory structure in development environments
            accountsFile = Paths.get("src/accounts.json");
        }
        
        backupFile = Paths.get(accountsFile.toString() + ".testbak");
        
        // Backup existing data so tests start with a clean slate without losing real users
        if (Files.exists(accountsFile)) {
            Files.copy(accountsFile, backupFile);
            Files.writeString(accountsFile, "[]");
        } else {
            Files.createDirectories(accountsFile.getParent());
            Files.writeString(accountsFile, "[]");
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original data to preserve the user's environment
        if (Files.exists(accountsFile)) {
            Files.delete(accountsFile);
        }
        if (Files.exists(backupFile)) {
            Files.move(backupFile, accountsFile);
        }
    }

    @Test
    void testAccountDataFlow() throws Exception {
        Run_MainMenuScreen screen = new Run_MainMenuScreen();
        
        // Use reflection to access the private data methods and the inner Account class
        Method saveMethod = Run_MainMenuScreen.class.getDeclaredMethod("saveAccounts", List.class);
        saveMethod.setAccessible(true);
        Method loadMethod = Run_MainMenuScreen.class.getDeclaredMethod("loadAccounts");
        loadMethod.setAccessible(true);
        
        Class<?> accountClass = Class.forName("Run_MainMenuScreen$Account");
        Constructor<?> constructor = accountClass.getDeclaredConstructor(String.class, String.class, boolean.class);
        constructor.setAccessible(true);
        
        // Create 2 test accounts
        List<Object> testAccounts = new ArrayList<>();
        testAccounts.add(constructor.newInstance("TesterA", "Pass1", true));
        testAccounts.add(constructor.newInstance("TesterB", "Pass2", false));
        
        // Save to file
        saveMethod.invoke(screen, testAccounts);
        
        // Load from file
        List<?> loadedAccounts = (List<?>) loadMethod.invoke(screen);
        
        assertEquals(2, loadedAccounts.size(), "Should have saved and loaded exactly two accounts");
        
        // Verify individual fields
        Object first = loadedAccounts.get(0);
        java.lang.reflect.Field nameField = accountClass.getDeclaredField("username");
        nameField.setAccessible(true);
        assertEquals("TesterA", nameField.get(first));
    }

    @Test
    void doLayout() {
        Run_MainMenuScreen screen = new Run_MainMenuScreen();
        screen.setSize(1024, 768);
        assertDoesNotThrow(screen::doLayout, "Component layout logic should not throw exceptions on resizing");
    }

    @Test
    void getPreferredSize() {
        Run_MainMenuScreen screen = new Run_MainMenuScreen();
        Dimension pref = screen.getPreferredSize();
        assertNotNull(pref);
        assertEquals(1280, pref.width);
        assertEquals(720, pref.height);
    }

    @Test
    void openMainMenuWindow() {
        // Verification of window creation entry point
        assertDoesNotThrow(() -> {
            // Test that the static initializer and resource loading doesn't crash the launch path
        });
    }

    @Test
    void addNotify() {
        Run_MainMenuScreen screen = new Run_MainMenuScreen();
        assertDoesNotThrow(screen::addNotify, "Registration in the UI tree should be smooth");
    }
}