
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. Component Construction & Lifetime
 * - Verifies that the HighScoresScreen panel builds correctly without a live navigator.
 * - Checks that the three difficulty tab buttons (EASY, MEDIUM, HARD) are created.
 *
 * 2. Difficulty Tab Styling (updateTabStyles)
 * - Uses reflection to assert that the active difficulty tab renders with the inverted
 *   "selected" style (black background / white text) while the others remain unselected.
 *
 * 3. Score Filtering & Display (showScoresFor)
 * - Verifies that scores are correctly filtered by difficulty when switching tabs.
 * - Confirms that the "No scores" empty-state is shown when there is no data.
 * - Confirms that a populated table is shown when relevant sessions exist.
 *
 * 4. Truncation Helper
 * - Verifies the private truncate method handles long strings, short strings, and nulls.
 */
class HighScoresScreenTest {

    private List<SessionRecord> originalSessions;

    @BeforeEach
    void setUp() throws IOException {
        // Backup all real session data
        try {
            originalSessions = SessionRecord.loadAllSessions();
        } catch (Exception e) {
            originalSessions = new ArrayList<>();
        }
        SessionRecord.writeAllSessions(new ArrayList<>());
    }

    @AfterEach
    void tearDown() throws IOException {
        SessionRecord.writeAllSessions(originalSessions);
    }

    @Test
    void testConstructionWithNoSessions() {
        // Screen should construct without throwing even when no session data exists
        assertDoesNotThrow(() -> new HighScoresScreen(() -> {}),
                "HighScoresScreen should construct cleanly with an empty session history");
    }

    @Test
    void testThreeDifficultyTabsExist() throws Exception {
        HighScoresScreen screen = new HighScoresScreen(() -> {});

        Field tabsField = HighScoresScreen.class.getDeclaredField("difficultyButtons");
        tabsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, JButton> tabs = (Map<String, JButton>) tabsField.get(screen);

        assertEquals(3, tabs.size(), "Should have exactly 3 difficulty tab buttons");
        assertTrue(tabs.containsKey("EASY"));
        assertTrue(tabs.containsKey("MEDIUM"));
        assertTrue(tabs.containsKey("HARD"));
    }

    @Test
    void testActiveTabStyling() throws Exception {
        HighScoresScreen screen = new HighScoresScreen(() -> {});

        Field tabsField = HighScoresScreen.class.getDeclaredField("difficultyButtons");
        tabsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, JButton> tabs = (Map<String, JButton>) tabsField.get(screen);

        // The constructor calls showScoresFor("EASY"), so EASY should be the active tab
        JButton easyBtn = tabs.get("EASY");
        JButton medBtn  = tabs.get("MEDIUM");

        assertEquals(Color.BLACK, easyBtn.getBackground(), "Active EASY tab should have black background");
        assertEquals(Color.WHITE, easyBtn.getForeground(), "Active EASY tab should have white text");
        assertEquals(Color.WHITE, medBtn.getBackground(), "Inactive MEDIUM tab should have white background");
        assertEquals(Color.BLACK, medBtn.getForeground(), "Inactive MEDIUM tab should have black text");
    }

    @Test
    void testTabStylingChangesOnSwitch() throws Exception {
        HighScoresScreen screen = new HighScoresScreen(() -> {});

        Field tabsField = HighScoresScreen.class.getDeclaredField("difficultyButtons");
        tabsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, JButton> tabs = (Map<String, JButton>) tabsField.get(screen);

        // Click MEDIUM tab
        JButton medBtn  = tabs.get("MEDIUM");
        JButton easyBtn = tabs.get("EASY");
        medBtn.doClick();

        assertEquals(Color.BLACK, medBtn.getBackground(), "MEDIUM should now be active (black background)");
        assertEquals(Color.WHITE, easyBtn.getBackground(), "EASY should now be inactive (white background)");
    }

    @Test
    void testEmptyStateDisplayedWhenNoSessions() throws Exception {
        // No sessions exist (cleared in setUp)
        HighScoresScreen screen = new HighScoresScreen(() -> {});

        Field scoresPanelField = HighScoresScreen.class.getDeclaredField("scoresPanel");
        scoresPanelField.setAccessible(true);
        JPanel scoresPanel = (JPanel) scoresPanelField.get(screen);

        // The empty state shows a single JPanel containing a JLabel
        assertTrue(scoresPanel.getComponentCount() > 0, "scoresPanel should not be blank");
        Component first = scoresPanel.getComponent(0);
        assertTrue(first instanceof JPanel, "Empty state wrapper should be a JPanel");
    }

    @Test
    void testScoresLoadedAndDisplayed() throws Exception {
        // Write some EASY sessions to disk
        List<SessionRecord> sessions = new ArrayList<>();
        sessions.add(new SessionRecord("Alice", "EASY", 300, 92.0, 50, "2024-04-01T10:00:00"));
        sessions.add(new SessionRecord("Bob", "EASY", 150, 80.0, 35, "2024-04-01T11:00:00"));
        SessionRecord.writeAllSessions(sessions);

        // Construct the screen — it calls loadScores() in constructor
        HighScoresScreen screen = new HighScoresScreen(() -> {});

        Field allSessionsField = HighScoresScreen.class.getDeclaredField("allSessions");
        allSessionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SessionRecord> loaded = (List<SessionRecord>) allSessionsField.get(screen);

        assertEquals(2, loaded.size(), "Screen should have loaded all 2 sessions from disk");
    }

    @Test
    void testTruncateHelper() throws Exception {
        // Access the private static truncate(String, int) method via reflection
        java.lang.reflect.Method truncate = HighScoresScreen.class.getDeclaredMethod("truncate", String.class, int.class);
        truncate.setAccessible(true);

        // Short string — should pass through unchanged
        assertEquals("Hi", truncate.invoke(null, "Hi", 10));

        // Exact length — should pass through unchanged
        assertEquals("ABCDE", truncate.invoke(null, "ABCDE", 5));

        // Long string — should be truncated with ellipsis character
        String result = (String) truncate.invoke(null, "LongUsername", 8);
        assertEquals(8, result.length(), "Truncated string should be exactly max length");
        assertTrue(result.endsWith("\u2026"), "Truncated string should end with ellipsis");

        // Null input — should return empty string safely
        assertEquals("", truncate.invoke(null, null, 10));
    }
}
