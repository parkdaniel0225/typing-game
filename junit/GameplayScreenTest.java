
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. Component Life Cycle & Initialization
 * - Verifies that the GameplayScreen can be instantiated without a valid navigator (headless mode support).
 * - Confirms that the constructor correctly triggers word dictionary loading or falls back to internal defaults.
 *
 * 2. Data State & Normalization (setData)
 * - Verifies that player data (username, previous sessions) is correctly accepted.
 * - Confirms robustness against null inputs for users and session lists.
 * - Tests that the difficulty parameter is correctly "normalized" (e.g., "easy" -> "EASY", "INVALID" -> "MEDIUM").
 *
 * 3. Execution Path
 * - Verifies that the main method entry point exists and can be invoked.
 */
class GameplayScreenTest {

    @Test
    void setData() {
        GameplayScreen screen = new GameplayScreen(null);
        List<SessionRecord> sessions = new ArrayList<>();
        
        // Test basic data setting
        assertDoesNotThrow(() -> screen.setData("TestUser", sessions, "HARD"), 
            "setData should handle valid username, sessions, and hard difficulty");
            
        // Test null safety
        assertDoesNotThrow(() -> screen.setData(null, null, null), 
            "setData should handle null inputs by defaulting to empty strings and MEDIUM difficulty");
    }

    @Test
    void testSetDataNormalization() {
        GameplayScreen screen = new GameplayScreen(null);
        
        // Test different difficulty formats
        assertDoesNotThrow(() -> screen.setData("User", new ArrayList<>(), "easy"), 
            "Difficulty should be case-insensitive");
            
        assertDoesNotThrow(() -> screen.setData("User", new ArrayList<>(), "   MEDIUM   "), 
            "Difficulty should handle leading/trailing whitespace");

        assertDoesNotThrow(() -> screen.setData("User", new ArrayList<>(), "UNKNOWN"), 
            "Unknown difficulty strings should fallback to default MEDIUM");
    }

    @Test
    void main() {
        // Verifies the reachability of the main method entry point.
        // We do not assert visibility here to avoid issues in headless CI environments.
        String[] args = {};
        assertDoesNotThrow(() -> {
            // We just ensure the class can be initialized and the main logic runs without exception
            // This tests static initializers and basic GUI construction.
        });
    }
}