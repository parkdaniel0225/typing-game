
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 *
 * 1. hasDictionaryEntry(String word)
 * - Validates guard clauses for null, empty, and whitespace-only strings (returns false).
 * - Performs live network checks for real words ("apple" -> true) and nonsense words (false).
 * - Verifies case-insensitivity (e.g., "APPLE" vs "apple").
 *
 * 2. fetchDefinition(String word)
 * - Ensures null/empty/blank inputs return null immediately without network calls.
 * - Live test: Confirms "apple" returns a non-blank string.
 * - Logic check: Ensures trailing/leading whitespace on input is trimmed and result is trimmed.
 * - Error handling: Returns null for nonsense words (simulating 404 responses).
 *
 * 3. extractFirstDefinition(String json)
 * - Offline logic tests for parsing the dictionary API's JSON structure.
 * - Success Path: Correctly extracts the first definition from nested JSON.
 * - Robustness: Handles missing keys, empty values, and null/empty JSON strings.
 * - Escaping/Encoding: Validates correct handling of escaped quotes (\"), newlines (\n),
 * tabs (\t), and Unicode sequences (e.g., \u0041).
 * - Constraints: Ensures only the first definition is returned when multiple exist and
 * treats whitespace-only definitions as null.
 */

class DefinitionLookupTest {

    @Test
    void hasDictionaryEntry_nullWord_returnsFalse() {
        assertFalse(DefinitionLookup.hasDictionaryEntry(null),
                "null input should return false");
    }

    @Test
    void hasDictionaryEntry_emptyString_returnsFalse() {
        assertFalse(DefinitionLookup.hasDictionaryEntry(""),
                "empty string should return false");
    }

    @Test
    void hasDictionaryEntry_blankString_returnsFalse() {
        assertFalse(DefinitionLookup.hasDictionaryEntry("   "),
                "whitespace-only string should return false");
    }

    @Test
    @Timeout(15)
        // network call – allow up to 15 seconds
    void hasDictionaryEntry_knownRealWord_returnsTrue() {
        // "apple" is in virtually every English dictionary – this should always pass.
        assertTrue(DefinitionLookup.hasDictionaryEntry("apple"),
                "'apple' must have a dictionary entry");
    }

    @Test
    @Timeout(15)
    void hasDictionaryEntry_nonsenseWord_returnsFalse() {
        // "xzqwvflkj" is extremely unlikely to exist in any dictionary.
        assertFalse(DefinitionLookup.hasDictionaryEntry("xzqwvflkj"),
                "nonsense word should not have a dictionary entry");
    }

    @Test
    @Timeout(15)
    void hasDictionaryEntry_isCaseInsensitive() {
        // The implementation lower-cases input; "APPLE" and "apple" should both work.
        assertTrue(DefinitionLookup.hasDictionaryEntry("APPLE"),
                "hasDictionaryEntry should be case-insensitive");
    }

    // -----------------------------------------------------------------------
    // fetchDefinition – tests guard clauses (no network) + live happy-path
    // -----------------------------------------------------------------------

    @Test
    void fetchDefinition_nullInput_returnsNull() {
        assertNull(DefinitionLookup.fetchDefinition(null),
                "null input must return null immediately");
    }

    @Test
    void fetchDefinition_emptyString_returnsNull() {
        assertNull(DefinitionLookup.fetchDefinition(""),
                "empty string must return null");
    }

    @Test
    void fetchDefinition_blankString_returnsNull() {
        assertNull(DefinitionLookup.fetchDefinition("   "),
                "blank / whitespace-only string must return null");
    }

    @Test
    @Timeout(15)
    void fetchDefinition_knownWord_returnsNonEmptyString() {
        String result = DefinitionLookup.fetchDefinition("apple");
        assertNotNull(result, "fetchDefinition('apple') must not return null");
        assertFalse(result.isBlank(), "fetchDefinition('apple') must return a non-blank definition");
    }

    @Test
    @Timeout(15)
    void fetchDefinition_knownWord_stripsLeadingTrailingWhitespace() {
        String result = DefinitionLookup.fetchDefinition("apple");
        assertNotNull(result);
        assertEquals(result.trim(), result,
                "Returned definition should not have leading/trailing whitespace");
    }

    @Test
    @Timeout(15)
    void fetchDefinition_inputWithExtraWhitespace_treatedSameAsCleanInput() {
        // The implementation trims the word before querying.
        String clean = DefinitionLookup.fetchDefinition("apple");
        String padded = DefinitionLookup.fetchDefinition("  apple  ");
        assertEquals(clean, padded,
                "Leading/trailing whitespace around the word should not change the result");
    }

    @Test
    @Timeout(15)
    void fetchDefinition_nonsenseWord_returnsNull() {
        assertNull(DefinitionLookup.fetchDefinition("xzqwvflkj"),
                "A nonsense word should return null (HTTP 404 → no definition)");
    }

    // -----------------------------------------------------------------------
    // extractFirstDefinition – pure logic, fully offline-testable
    // -----------------------------------------------------------------------

    /**
     * Minimal JSON structure matching what the free-dictionary API returns.
     */
    private static final String SAMPLE_JSON =
            "[{\"word\":\"apple\",\"meanings\":[{\"definitions\":[" +
                    "{\"definition\":\"A common, round fruit produced by the tree.\"}]}]}]";

    @Test
    void extractFirstDefinition_validJson_returnsDefinition() {
        String result = DefinitionLookup.extractFirstDefinition(SAMPLE_JSON);
        assertNotNull(result, "Should extract a definition from valid JSON");
        assertEquals("A common, round fruit produced by the tree.", result);
    }

    @Test
    void extractFirstDefinition_noDefinitionKey_returnsNull() {
        String json = "[{\"word\":\"apple\",\"meanings\":[]}]";
        assertNull(DefinitionLookup.extractFirstDefinition(json),
                "JSON without a 'definition' key should return null");
    }

    @Test
    void extractFirstDefinition_emptyDefinitionValue_returnsNull() {
        // The value for "definition" is an empty string – result should be null.
        String json = "[{\"definition\":\"\"}]";
        assertNull(DefinitionLookup.extractFirstDefinition(json),
                "Empty definition string should return null");
    }

    @Test
    void extractFirstDefinition_nullJson_returnsNull() {
        assertNull(DefinitionLookup.extractFirstDefinition(null),
                "null JSON string should return null");
    }

    @Test
    void extractFirstDefinition_emptyString_returnsNull() {
        assertNull(DefinitionLookup.extractFirstDefinition(""),
                "Empty JSON string should return null");
    }

    @Test
    void extractFirstDefinition_handlesEscapedQuoteInDefinition() {
        // A definition that contains an escaped double-quote: He said \"hello\".
        String json = "[{\"definition\":\"He said \\\"hello\\\".\"}]";
        String result = DefinitionLookup.extractFirstDefinition(json);
        assertNotNull(result);
        assertEquals("He said \"hello\".", result,
                "Escaped quotes inside the definition value should be unescaped");
    }

    @Test
    void extractFirstDefinition_handlesEscapedNewlineInDefinition() {
        String json = "[{\"definition\":\"Line one.\\nLine two.\"}]";
        String result = DefinitionLookup.extractFirstDefinition(json);
        assertNotNull(result);
        assertTrue(result.contains("\n"),
                "Escaped '\\n' in the JSON should become a real newline character");
    }

    @Test
    void extractFirstDefinition_handlesEscapedTabInDefinition() {
        String json = "[{\"definition\":\"col1\\tcol2\"}]";
        String result = DefinitionLookup.extractFirstDefinition(json);
        assertNotNull(result);
        assertTrue(result.contains("\t"),
                "Escaped '\\t' in the JSON should become a real tab character");
    }

    @Test
    void extractFirstDefinition_handlesUnicodeEscape() {
        // \u0041 is 'A' in Unicode.
        String json = "[{\"definition\":\"\\u0041 letter.\"}]";
        String result = DefinitionLookup.extractFirstDefinition(json);
        assertNotNull(result);
        assertTrue(result.startsWith("A"),
                "Unicode escape \\u0041 should be decoded to 'A'");
    }

    @Test
    void extractFirstDefinition_returnsOnlyFirstDefinitionWhenMultipleExist() {
        String json = "[{\"definition\":\"First def.\"},{\"definition\":\"Second def.\"}]";
        String result = DefinitionLookup.extractFirstDefinition(json);
        assertEquals("First def.", result,
                "Only the first 'definition' value should be returned");
    }

    @Test
    void extractFirstDefinition_whitespaceOnlyValue_returnsNull() {
        // After trimming, should be treated as empty → null.
        String json = "[{\"definition\":\"   \"}]";
        assertNull(DefinitionLookup.extractFirstDefinition(json),
                "A whitespace-only definition value should return null after trimming");
    }
}