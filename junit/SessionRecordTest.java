
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. File Persistence (loadAllSessions / writeAllSessions)
 * - Verifies that a list of SessionRecord objects is correctly serialized to JSON and deserialized back.
 * - Confirms data integrity for username, score, accuracy, WPM, and dates.
 *
 * 2. High Score Logic (appendSessionToFile)
 * - Verifies that adding a new session correctly integrates it into the persistent file.
 * - Confirms that only the TOP 10 scores per user are kept (sorting by score DESC, then date DESC).
 *
 * 3. Data Integrity & Formatting
 * - Validates JSON escaping/unescaping (handling quotes, backslashes, tabs).
 * - Checks formatted date strings for display.
 *
 * 4. Object Mechanics
 * - Standard tests for equals, hashCode, and toString.
 */
class SessionRecordTest {

    private List<SessionRecord> originalSessions;

    @BeforeEach
    void setUp() throws IOException {
        // Backup original data using the public API before running tests
        try {
            originalSessions = SessionRecord.loadAllSessions();
        } catch (Exception e) {
            originalSessions = new ArrayList<>();
        }
        // Start each test with an empty sessions list
        SessionRecord.writeAllSessions(new ArrayList<>());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original data after each test
        SessionRecord.writeAllSessions(originalSessions);
    }

    @Test
    void loadAllSessions() throws IOException {
        List<SessionRecord> sessions = new ArrayList<>();
        sessions.add(new SessionRecord("UserA", "Hard", 1200, 98.5, 75, "2024-04-01T12:00:00"));
        sessions.add(new SessionRecord("UserB", "Easy", 450, 75.0, 35, "2024-04-01T12:05:00"));

        SessionRecord.writeAllSessions(sessions);
        List<SessionRecord> loaded = SessionRecord.loadAllSessions();

        assertEquals(2, loaded.size());
        assertEquals("UserA", loaded.get(0).getUsername());
        assertEquals(1200, loaded.get(0).getScore());
        assertEquals(98.5, loaded.get(0).getAccuracy());
        assertEquals("2024-04-01T12:00:00", loaded.get(0).getDate());
    }

    @Test
    void writeAllSessions() throws IOException {
        // Test robustness against characters that need JSON escaping
        List<SessionRecord> sessions = new ArrayList<>();
        sessions.add(new SessionRecord("Tester\"Quotes\"", "Hard\tTab", 100, 100.0, 100, "2024-01-01T00:00:00"));

        SessionRecord.writeAllSessions(sessions);
        List<SessionRecord> loaded = SessionRecord.loadAllSessions();

        assertEquals(1, loaded.size());
        assertEquals("Tester\"Quotes\"", loaded.get(0).getUsername());
        assertEquals("Hard\tTab", loaded.get(0).getDifficulty());
    }

    @Test
    void appendSessionToFile() throws IOException {
        String testUser = "ConsistencyTest";
        
        // Write 12 sessions for the same user with increasing scores
        for (int i = 1; i <= 12; i++) {
            SessionRecord r = new SessionRecord(testUser, "Normal", i * 10, 90.0, 45, "2024-03-01T10:00:" + String.format("%02d", i));
            SessionRecord.appendSessionToFile(r);
        }

        List<SessionRecord> all = SessionRecord.loadAllSessions();
        
        // Assert that the system only kept the top 10 sessions for this user
        assertEquals(10, all.size(), "Only top 10 sessions should be kept per user");

        // The highest score (12th insertion) should be at the top
        assertEquals(120, all.get(0).getScore());
        
        // The lowest scores (10 and 20) should have been truncated
        boolean containsScore20 = all.stream().anyMatch(s -> s.getScore() == 20);
        assertFalse(containsScore20, "Score of 20 should have been filtered out in favor of better scores");
        
        // Verify sorting by date for equal scores
        SessionRecord equal1 = new SessionRecord(testUser, "Normal", 500, 90.0, 45, "2024-03-01T12:00:00");
        SessionRecord equal2 = new SessionRecord(testUser, "Normal", 500, 90.0, 45, "2024-03-01T13:00:00");
        SessionRecord.writeAllSessions(new ArrayList<>()); // Clear
        SessionRecord.appendSessionToFile(equal1);
        SessionRecord.appendSessionToFile(equal2);
        
        List<SessionRecord> loadedEqual = SessionRecord.loadAllSessions();
        assertEquals("2024-03-01T13:00:00", loadedEqual.get(0).getDate(), "For equal scores, the most recent session should be first");
    }

    @Test
    void testEquals() {
        SessionRecord r1 = new SessionRecord("User", "Diff", 100, 90.0, 50, "now");
        SessionRecord r2 = new SessionRecord("User", "Diff", 100, 90.0, 50, "now");
        SessionRecord r3 = new SessionRecord("Other", "Diff", 100, 90.0, 50, "now");

        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertNotEquals(r1, null);
        assertNotEquals(r1, "not a record");
    }

    @Test
    void testHashCode() {
        SessionRecord r1 = new SessionRecord("User", "Diff", 100, 90.0, 50, "now");
        SessionRecord r2 = new SessionRecord("User", "Diff", 100, 90.0, 50, "now");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        SessionRecord r = new SessionRecord("SampleUser", "Extreme", 999, 100.0, 150, "2024-05-01");
        String str = r.toString();
        assertTrue(str.contains("SampleUser"));
        assertTrue(str.contains("999"));
        assertTrue(str.contains("Extreme"));
    }
    
    @Test
    void getFormattedDate() {
        // Test valid ISO date
        SessionRecord r1 = new SessionRecord("u", "d", 0, 0.0, 0, "2024-12-25T15:45:00");
        assertEquals("2024-12-25 03:45 PM", r1.getFormattedDate());
        
        // Test fallback for invalid date string
        SessionRecord r2 = new SessionRecord("u", "d", 0, 0.0, 0, "yesterday");
        assertEquals("yesterday", r2.getFormattedDate());
    }
}