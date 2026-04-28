
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. Lifecycle & File Management (setUp/tearDown)
 * - Implements a backup system to preserve existing "game_settings.json" files during testing.
 * - Ensures the environment is cleaned up and original settings are restored after each test run.
 *
 * 2. load() Functionality
 * - Default State: Verifies that a missing settings file results in default values (3 starting lives, 2 extra lives, power-ups enabled).
 * - JSON Parsing: Confirms that valid JSON data is correctly mapped to class properties.
 * - Legacy Support: Checks backward compatibility for the "maxTotalLives" field, ensuring it correctly calculates "maxExtraLives" (Total - Starting).
 *
 * 3. save() Functionality
 * - Persistence: Verifies that modified settings are correctly serialized and written to the local file system.
 * - Data Integrity: Confirms the presence of specific keys and values within the generated JSON string.
 *
 * 4. Value Clamping & Validation
 * - Range Enforcement: Tests that starting lives are clamped between a minimum of 1 and a maximum of 99.
 * - Sum Constraints: Validates logic that ensures the sum of starting lives and extra lives does not exceed a total of 99.
 */

class GameSettingsTest {

    private static final Path TEST_FILE = Paths.get("game_settings.json");
    private Path backupFile = Paths.get("game_settings.json.bak");

    @BeforeEach
    void setUp() throws IOException {
        // Backup existing settings file if it exists
        if (Files.exists(TEST_FILE)) {
            Files.copy(TEST_FILE, backupFile);
            Files.delete(TEST_FILE);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore existing settings file if backup exists
        Files.deleteIfExists(TEST_FILE);
        if (Files.exists(backupFile)) {
            Files.move(backupFile, TEST_FILE);
        }
    }

    @Test
    void load() throws IOException {
        // Test 1: Load default when file missing
        GameSettings defaults = GameSettings.load();
        assertEquals(3, defaults.getStartingLives());
        assertEquals(2, defaults.getMaxExtraLives());
        assertTrue(defaults.isPowerUpsEnabled());

        // Test 2: Load valid JSON
        String json = "{\n" +
                "  \"startingLives\": 5,\n" +
                "  \"maxExtraLives\": 10,\n" +
                "  \"powerUpsEnabled\": false\n" +
                "}\n";
        Files.writeString(TEST_FILE, json, StandardCharsets.UTF_8);

        GameSettings loaded = GameSettings.load();
        assertEquals(5, loaded.getStartingLives());
        assertEquals(10, loaded.getMaxExtraLives());
        assertFalse(loaded.isPowerUpsEnabled());
    }

    @Test
    void loadLegacy() throws IOException {
        // Test support for legacy "maxTotalLives" field
        // Formula: s.maxExtraLives = Math.max(0, total - s.startingLives);
        String legacyJson = "{\n" +
                "  \"startingLives\": 3,\n" +
                "  \"maxTotalLives\": 10\n" +
                "}\n";
        Files.writeString(TEST_FILE, legacyJson, StandardCharsets.UTF_8);

        GameSettings loaded = GameSettings.load();
        assertEquals(7, loaded.getMaxExtraLives()); // 10 - 3 = 7
    }

    @Test
    void save() throws IOException {
        GameSettings s = new GameSettings();
        s.setStartingLives(10);
        s.setMaxExtraLives(5);
        s.setPowerUpsEnabled(false);

        s.save();

        assertTrue(Files.exists(TEST_FILE));
        String json = Files.readString(TEST_FILE, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"startingLives\": 10"));
        assertTrue(json.contains("\"maxExtraLives\": 5"));
        assertTrue(json.contains("\"powerUpsEnabled\": false"));
    }

    @Test
    void clamping() throws IOException {
        GameSettings s = new GameSettings();
        
        // Test starting lives min clamp
        s.setStartingLives(0);
        s.save(); // save calls clamp()
        assertEquals(1, GameSettings.load().getStartingLives());

        // Test starting lives max clamp
        s.setStartingLives(200);
        s.save();
        assertEquals(99, GameSettings.load().getStartingLives());

        // Test sum clamp (startingLives + maxExtraLives <= 99)
        s.setStartingLives(90);
        s.setMaxExtraLives(20); // 90 + 20 = 110 > 99
        s.save(); // clamp sets maxExtraLives = 99 - 90 = 9
        
        GameSettings loaded = GameSettings.load();
        assertEquals(90, loaded.getStartingLives());
        assertEquals(9, loaded.getMaxExtraLives());
        assertEquals(99, loaded.getMaxTotalLives());
    }
}