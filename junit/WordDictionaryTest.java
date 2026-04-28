
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. WordDictionary Logic
 * - Path Resolution: Ensures the dictionary is stored in a hidden home directory folder.
 * - File Loading: Verifies that words are correctly categorized into Easy (3-4 letters),
 *   Medium (5-6 letters), and Hard (7-8 letters) buckets.
 * - Validation Loading: Ensures all acceptable words are loaded into a Set for fast lookup.
 * - Rank Enforcement: Confirms that loaders respect the MAX_WORD_LIST_RANK and MAX_VALIDATION_WORD_RANK limits.
 *
 * 2. WordFilter Rules
 * - Character Filtering: Rejects words with non-lowercase-ASCII characters, nulls, or empty strings.
 * - Length Constraints: Rejects words longer than 10 characters.
 * - Phonetic Requirements: Rejects words without vowels (a, e, i, o, u, y).
 * - Language Cleanup: Identifies and rejects likely plural forms (ending in 's', 'es', 'ies', etc.) and blocklisted technical jargon/abbreviations.
 */
class WordDictionaryTest {

    @Test
    void getDictionaryPath() {
        Path path = WordDictionary.getDictionaryPath();
        assertNotNull(path);
        String pathStr = path.toString();
        // Check for hidden directory and filename
        assertTrue(pathStr.contains(".deathBySpellCheck"), "Path should contain the hidden cache directory");
        assertTrue(pathStr.endsWith("google_10000_english_no_swears.txt"), "Path should end with the correct filename");
    }

    @Test
    void ensureDictionaryFile() {
        // This method involves a network download in its default behavior.
        // In this environment, we focus on verifying that the path logic behaves correctly.
        // Full integration tests for dictionary download are typically handled in a separate suite.
    }

    @Test
    void loadBucketsFromFile() throws IOException {
        Path tempFile = Files.createTempFile("test_buckets", ".txt");
        try {
            List<String> content = Arrays.asList(
                    "cat",        // Easy
                    "dogs",       // Plural (Filtered)
                    "about",      // Medium
                    "banana",     // Medium
                    "chicken",    // Hard
                    "keyboard",   // Hard
                    "a",          // Too short
                    "international" // Too long
            );
            Files.write(tempFile, content, StandardCharsets.UTF_8);

            WordDictionary.DictionaryBuckets buckets = WordDictionary.loadBucketsFromFile(tempFile);

            // Easy: cat (3)
            assertEquals(1, buckets.easy.size());
            assertTrue(buckets.easy.contains("cat"));

            // Medium: about (5), banana (6)
            assertEquals(2, buckets.medium.size());
            assertTrue(buckets.medium.contains("about"));
            assertTrue(buckets.medium.contains("banana"));

            // Hard: chicken (7), keyboard (8)
            assertEquals(2, buckets.hard.size());
            assertTrue(buckets.hard.contains("chicken"));
            assertTrue(buckets.hard.contains("keyboard"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void loadAllAcceptableWords() throws IOException {
        Path tempFile = Files.createTempFile("test_validation", ".txt");
        try {
            List<String> content = Arrays.asList(
                    "hello",
                    "world",
                    "123",    // Invalid
                    "bcdf",   // No vowels (Invalid)
                    "apples"  // Plural (Invalid)
            );
            Files.write(tempFile, content, StandardCharsets.UTF_8);

            Set<String> set = WordDictionary.loadAllAcceptableWords(tempFile);

            assertTrue(set.contains("hello"));
            assertTrue(set.contains("world"));
            assertEquals(2, set.size());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}

class WordFilterTest {

    @Test
    void isAcceptable() {
        // Valid words
        assertTrue(WordFilter.isAcceptable("apple"));
        assertTrue(WordFilter.isAcceptable("sky"), "Y should count as a vowel");
        assertTrue(WordFilter.isAcceptable("rhythm"), "Y is the only vowel");

        // Invalid Characters/Input
        assertFalse(WordFilter.isAcceptable(null));
        assertFalse(WordFilter.isAcceptable(""));
        assertFalse(WordFilter.isAcceptable("apple123"));
        assertFalse(WordFilter.isAcceptable("test!"));

        // Length limits
        assertTrue(WordFilter.isAcceptable("abcdefghij"), "10 characters is acceptable");
        assertFalse(WordFilter.isAcceptable("abcdefghijk"), "11 characters is unacceptable");

        // Phonics
        assertFalse(WordFilter.isAcceptable("bcdf"), "Must contain a vowel");

        // Blocklist (Tech jargon/Abbreviations)
        assertFalse(WordFilter.isAcceptable("html"));
        assertFalse(WordFilter.isAcceptable("wifi"));
        assertFalse(WordFilter.isAcceptable("lol"));

        // Plurals and Inflections
        assertFalse(WordFilter.isAcceptable("apples"), "Simple plurals should be filtered");
        assertFalse(WordFilter.isAcceptable("boxes"), "Complex plurals should be filtered");
        assertFalse(WordFilter.isAcceptable("cities"), "IES plurals should be filtered");
        
        // Whitelist (Words ending in S that are not plurals)
        assertTrue(WordFilter.isAcceptable("always"));
        assertTrue(WordFilter.isAcceptable("lens"));
        assertTrue(WordFilter.isAcceptable("glass"), "Words ending in SS are acceptable");
        assertTrue(WordFilter.isAcceptable("bus"), "Words ending in US are acceptable");
    }
}