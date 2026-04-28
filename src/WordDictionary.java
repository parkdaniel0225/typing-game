import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Manages the external word list used for gameplay
 * 
 * @author Daniel Park
 */


public final class WordDictionary {

    public static final String DOWNLOAD_URL =
            "https://raw.githubusercontent.com/first20hours/google-10000-english/master/google-10000-english-no-swears.txt";

    public static final int MAX_WORD_LIST_RANK = 2000;

    public static final int MAX_VALIDATION_WORD_RANK = 10000;

    private static final String CACHE_DIR_NAME = ".deathBySpellCheck";
    private static final String DICT_FILE_NAME = "google_10000_english_no_swears.txt";

    private WordDictionary() {
    }

    /**
     * Constructs the local file path for the dictionary
     * 
     * @return The {@link Path} to the dictionary file
    */
    public static Path getDictionaryPath() {
        return Paths.get(System.getProperty("user.home"), CACHE_DIR_NAME, DICT_FILE_NAME);
    }

    /**
     * Checks if Dictionary exists, else makes new one
     * 
     * @throws IOException If the directory or file cannot be created or accessed
     */
    public static void ensureDictionaryFile() throws IOException {
        Path path = getDictionaryPath();
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.isRegularFile(path) || Files.size(path) == 0L) {
            downloadUsingStream(DOWNLOAD_URL, path.toFile());
        }
    }

    /**
     * Downloads a file from a URL
     * 
     * @param urlStr The source URL
     * @param dest   The destination {@link File} on the local system
     * @throws IOException If a network or disk error occurs during the download
     */
    private static void downloadUsingStream(String urlStr, File dest) throws IOException {
        URI uri = URI.create(urlStr);
        try (BufferedInputStream bis = new BufferedInputStream(uri.toURL().openStream());
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
        }
    }

    /**
     * Reads the dictionary file, finding the difficulty words lists
     * 
     * @param file The path to the dictionary file
     * @return A {@code DictionaryBuckets} containing the categorized word lists
     * @throws IOException If the file cannot be read
     */
    public static DictionaryBuckets loadBucketsFromFile(Path file) throws IOException {
        List<String> easy = new ArrayList<>();
        List<String> medium = new ArrayList<>();
        List<String> hard = new ArrayList<>();

        try (java.io.BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount > MAX_WORD_LIST_RANK) {
                    break;
                }
                String w = line.trim().toLowerCase(Locale.ENGLISH);
                if (!WordFilter.isAcceptable(w)) {
                    continue;
                }
                int len = w.length();
                if (len >= 3 && len <= 4) {
                    easy.add(w);
                } else if (len >= 5 && len <= 6) {
                    medium.add(w);
                } else if (len >= 7 && len <= 8) {
                    hard.add(w);
                }
            }
        }
        return new DictionaryBuckets(easy, medium, hard);
    }

    /**
     * Loads the acceptable words into a set
     * 
     * @param file The path to the dictionary file
     * @return A {@link Set} of lowercase acceptable words
     * @throws IOException If the file cannot be read
     */
    public static Set<String> loadAllAcceptableWords(Path file) throws IOException {
        HashSet<String> set = new HashSet<>();
        try (java.io.BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount > MAX_VALIDATION_WORD_RANK) {
                    break;
                }
                String w = line.trim().toLowerCase(Locale.ENGLISH);
                if (w.isEmpty() || !WordFilter.isAcceptable(w)) {
                    continue;
                }
                set.add(w);
            }
        }
        return set;
    }

    /**
     * Class for holding the categorized dictionary words.
     */
    public static final class DictionaryBuckets {
        public final List<String> easy;
        public final List<String> medium;
        public final List<String> hard;

        /**
         * Creates a holder populated per difficulty tier.
         *
         * @param easy   list of easy words
         * @param medium list of medium words
         * @param hard   list of hard words
         */
        public DictionaryBuckets(List<String> easy, List<String> medium, List<String> hard) {
            this.easy = easy;
            this.medium = medium;
            this.hard = hard;
        }
    }
}
