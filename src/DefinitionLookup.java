import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * A utility class that is used to look up definitions for words from the Dictionary
 * 
 * @author Boyu Yang
 */

public final class DefinitionLookup {

    private static final String API_BASE =
            "https://api.dictionaryapi.dev/api/v2/entries/en/";

    private DefinitionLookup() {
    }

    /**
     * Checks if a specific word has an entry in the Dictionary
     * 
     * @param word The word to check for
     * @return A Bool, true if word was found
     */
    public static boolean hasDictionaryEntry(String word) {
        String def = fetchDefinition(word);
        return def != null && !def.trim().isEmpty();
    }

    /**
     * Gets the specified word defintion
     * 
     * @param word The word to look up
     * @return A String with the first definition found
     */
    public static String fetchDefinition(String word) {
        if (word == null) {
            return null;
        }
        String clean = word.trim().toLowerCase(Locale.ENGLISH);
        if (clean.isEmpty()) {
            return null;
        }

        try {
            String urlStr = API_BASE + URLEncoder.encode(clean, StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "DeathBySpellCheck/1.0");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code != 200) {
                return null;
            }

            StringBuilder body = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    body.append(line);
                }
            }
            return extractFirstDefinition(body.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses return vale from Dictionary
     * 
     * @param json The JSON returned from the dictionary
     * @return A String containg the first definition
     */
    static String extractFirstDefinition(String json) {
        if (json == null) {
            return null;
        }
        String key = "\"definition\":\"";
        int idx = json.indexOf(key);
        if (idx < 0) {
            return null;
        }
        int i = idx + key.length();
        StringBuilder sb = new StringBuilder();
        while (i < json.length()) {
            char ch = json.charAt(i);
            if (ch == '\\' && i + 1 < json.length()) {
                char n = json.charAt(i + 1);
                if (n == '"' || n == '\\' || n == '/') {
                    sb.append(n);
                    i += 2;
                    continue;
                }
                if (n == 'n') {
                    sb.append('\n');
                    i += 2;
                    continue;
                }
                if (n == 'r') {
                    sb.append('\r');
                    i += 2;
                    continue;
                }
                if (n == 't') {
                    sb.append('\t');
                    i += 2;
                    continue;
                }
                if (n == 'u' && i + 5 < json.length()) {
                    try {
                        int cp = Integer.parseInt(json.substring(i + 2, i + 6), 16);
                        sb.append((char) cp);
                    } catch (NumberFormatException ex) {
                        sb.append('?');
                    }
                    i += 6;
                    continue;
                }
            }
            if (ch == '"') {
                break;
            }
            sb.append(ch);
            i++;
        }
        String s = sb.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
