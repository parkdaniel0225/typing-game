import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single gameplay session's results
 * 
 * @author Daniel Park
 */


public class SessionRecord {

    private static final Path SESSIONS_FILE;

    static {
        Path dir;
        try {
            java.net.URL url = SessionRecord.class.getResource("sessions.json");
            if (url != null) {
                dir = Paths.get(url.toURI()).getParent();
            } else {
                dir = Paths.get(SessionRecord.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            }
        } catch (Exception e) {
            dir = Paths.get(".");
        }
        SESSIONS_FILE = dir.resolve("sessions.json");
    }

    private static final Pattern SESSION_PATTERN = Pattern.compile(
            "\\{\\s*\"username\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*"
                    + "\"difficulty\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*"
                    + "\"score\"\\s*:\\s*(\\d+)\\s*,\\s*"
                    + "\"accuracy\"\\s*:\\s*([0-9.]+)\\s*,\\s*"
                    + "\"wpm\"\\s*:\\s*(\\d+)\\s*,\\s*"
                    + "\"date\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*\\}"
    );

    private String username;
    private String difficulty;
    private int score;
    private double accuracy;
    private int wpm;
    private String date;

    /**
     * SessionRecord Constructor
     * 
     * @param username The name of the player
     * @param difficulty The difficulty level played
     * @param score The total points earned
     * @param accuracy The percentage of correct keystrokes
     * @param wpm The calculated Words Per Minute
     * @param date The timestamp of the session
     */
    public SessionRecord(String username, String difficulty, int score, double accuracy, int wpm, String date) {
        this.username = username;
        this.difficulty = difficulty;
        this.score = score;
        this.accuracy = accuracy;
        this.wpm = wpm;
        this.date = date;
    }

    /** Gets username */
    public String getUsername() {
        return username;
    }

    /** Gets difficulty */
    public String getDifficulty() {
        return difficulty;
    }

    /** Gets score */
    public int getScore() {
        return score;
    }

    /** Gets accuracy */
    public double getAccuracy() {
        return accuracy;
    }

    /** Gets WPM */
    public int getWpm() {
        return wpm;
    }

    /** Gets date */
    public String getDate() {
        return date;
    }

    /**
     * Formats the date
     * 
     * @return A String formated as (yyyy-MM-dd hh:mm a)
     */
    public String getFormattedDate() {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date);
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
            return dateTime.format(formatter);
        } catch (Exception e) {
            return date;
        }
    }

    /**
     * Gets all session records
     * 
     * @return A list of all the records
     * @throws IOException If the file exists but cannot be read
     */
    public static List<SessionRecord> loadAllSessions() throws IOException {
        return readSessionsFromFile();
    }

    /**
     * Reads session file
     * 
     * @return A list of all the records
     * @throws IOException If the file exists but cannot be read
     */
    private static List<SessionRecord> readSessionsFromFile() throws IOException {
        List<SessionRecord> list = new ArrayList<>();
        if (!Files.exists(SESSIONS_FILE)) {
            return list;
        }
        String content = Files.readString(SESSIONS_FILE, StandardCharsets.UTF_8);
        Matcher m = SESSION_PATTERN.matcher(content);
        while (m.find()) {
            String username = unescapeJson(m.group(1));
            String difficulty = unescapeJson(m.group(2));
            int score = Integer.parseInt(m.group(3));
            double accuracy = Double.parseDouble(m.group(4));
            int wpm = Integer.parseInt(m.group(5));
            String date = unescapeJson(m.group(6));
            list.add(new SessionRecord(username, difficulty, score, accuracy, wpm, date));
        }
        return list;
    }

    /**
     * Overwrites the session file with the provided list of records
     * 
     * @param sessions The list of all sessions
     * @throws IOException If the file exists but cannot be written to
     */
    public static void writeAllSessions(List<SessionRecord> sessions) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < sessions.size(); i++) {
            SessionRecord r = sessions.get(i);
            json.append(formatJsonEntry(r));
            if (i < sessions.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");
        Files.writeString(SESSIONS_FILE, json.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Saves a new session record to the file
     * 
     * @param record The new {@link SessionRecord} to be saved
     * @throws IOException If file access fails
     */
    public static void appendSessionToFile(SessionRecord record) throws IOException {
        ensureJsonFileExists(SESSIONS_FILE);

        List<SessionRecord> allSessions = readSessionsFromFile();
        allSessions.add(record);

        java.util.Map<String, List<SessionRecord>> byUser = new java.util.HashMap<>();
        for (SessionRecord r : allSessions) {
            String u = r.getUsername() != null ? r.getUsername().toLowerCase(Locale.ENGLISH) : "";
            byUser.computeIfAbsent(u, k -> new ArrayList<>()).add(r);
        }

        List<SessionRecord> filteredSessions = new ArrayList<>();
        java.util.Comparator<SessionRecord> order = java.util.Comparator
                .comparingInt(SessionRecord::getScore).reversed()
                .thenComparing(SessionRecord::getDate, java.util.Comparator.reverseOrder());

        for (List<SessionRecord> userSessions : byUser.values()) {
            userSessions.sort(order);
            if (userSessions.size() > 10) {
                userSessions = userSessions.subList(0, 10);
            }
            filteredSessions.addAll(userSessions);
        }

        writeAllSessions(filteredSessions);
    }

    /**
     * Ensures the save file exists, else makes new one
     * 
     * @param path The file path to check
     * @throws IOException If file creation fails
     */
    private static void ensureJsonFileExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.writeString(path, "[]\n", StandardCharsets.UTF_8);
        }
    }

    /**
     * Converts a to a formatted String
     * 
     * @param record A {@link SessionRecord}
     * @return A String formatted
    */
    private static String formatJsonEntry(SessionRecord record) {
        return "  {\n"
                + "    \"username\": \"" + escapeJson(record.getUsername()) + "\",\n"
                + "    \"difficulty\": \"" + escapeJson(record.getDifficulty()) + "\",\n"
                + "    \"score\": " + record.getScore() + ",\n"
                + "    \"accuracy\": " + String.format(Locale.US, "%.2f", record.getAccuracy()) + ",\n"
                + "    \"wpm\": " + record.getWpm() + ",\n"
                + "    \"date\": \"" + escapeJson(record.getDate()) + "\"\n"
                + "  }";
    }

    /** Text Formating */
    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Text Formating */
    private static String unescapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SessionRecord other)) {
            return false;
        }
        return score == other.score
                && Double.compare(accuracy, other.accuracy) == 0
                && wpm == other.wpm
                && Objects.equals(username, other.username)
                && Objects.equals(difficulty, other.difficulty)
                && Objects.equals(date, other.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, difficulty, score, accuracy, wpm, date);
    }

    @Override
    public String toString() {
        return "SessionRecord{"
                + "username='" + username + '\''
                + ", difficulty='" + difficulty + '\''
                + ", score=" + score
                + ", accuracy=" + accuracy
                + ", wpm=" + wpm
                + ", date='" + date + '\''
                + '}';
    }
}
