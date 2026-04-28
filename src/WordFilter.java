import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Filters out acronyms to ensure a smoother user gameplay experience
 * 
 * @author Daniel Park
 */


public final class WordFilter {

    private static final Set<Character> VOWELS = new HashSet<>(Arrays.asList(
            'a', 'e', 'i', 'o', 'u', 'y'));

    private static final Set<String> BLOCKLIST = new HashSet<>(Arrays.asList(
            "acc", "des", "pct", "qty", "tsp", "tbsp", "oz", "mph", "kph", "rpm", "lbs",
            "ans", "avg", "def", "ref", "alt", "esp", "std", "num", "amt",
            "bal", "seq", "tmp", "var", "org", "usr", "sys", "dir", "cmd",
            "dll", "exe", "pdf", "url", "html", "xml", "css", "sql", "api",
            "gpu", "cpu", "ram", "ios", "ads", "seo", "ceo", "cfo", "dpi",
            "usb", "dvd", "hdr", "lcd", "gps", "gsm", "lte", "vpn", "wifi",
            "etc", "ltd", "inc", "llc", "corp", "dept", "approx", "misc",
            "fax", "tel", "msg", "vol", "pp", "abbr", "acct", "cfg", "chk",
            "ctrl", "dbg", "ptr", "stmt", "txn", "intf", "xor", "min", "max",
            "aux", "elem", "env", "idx", "init", "iter", "loc", "meta", "opt",
            "param", "rect", "reg", "rng", "svc", "viz", "yrs",
            "isbn", "issn", "doi", "sku", "upc", "ean", "asin", "isrc", "uuid",
            "http", "https", "www", "ftp", "smtp", "tcp", "udp", "ip", "dns",
            "jpg", "png", "gif", "svg", "mp3", "mp4", "wav", "avi", "iso",
            "nfc", "rfid", "hdmi", "oled", "led",
            "faq", "qna", "diy", "gui", "ide", "sdk", "svn", "git", "vim", "aws",
            "json", "yaml", "csv", "ajax", "nginx", "iot", "ipv", "lan", "pcs",
            "xls", "ppt", "txt", "deb", "apt", "yum", "gnu", "bsd", "gpl", "lgpl",
            "lol", "omg", "wtf", "fyi", "imo", "btw", "tbh", "idk", "ikr", "dm",
            "gmt", "utc", "pst", "cst", "mst", "olas"
    ));

    private static final Set<String> S_WORD_SINGULAR_WHITELIST = new HashSet<>(Arrays.asList(
            "always", "says", "news", "lens", "sometimes"
    ));

    private WordFilter() {
    }

    /**
     * Determines if a word is suitable for use in the game 
     * Checks include:
     * <ul>
     * <li>Null and empty string checks</li>
     * <li>Regex validation for alphabetic characters only</li>
     * <li>Maximum length constraint (10 characters)</li>
     * <li>Presence of at least one vowel</li>
     * <li>Verification against the {@link #BLOCKLIST}</li>
     * <li>Heuristic checks for plural or inflected forms</li>
     * </ul>
     * 
     * @param word The candidate string to validate.
     * @return A bool, true if word meets all criteria
    */
    public static boolean isAcceptable(String word) {
        if (word == null) {
            return false;
        }
        String w = word.trim().toLowerCase(Locale.ENGLISH);
        if (w.isEmpty() || !w.matches("[a-z]+")) {
            return false;
        }
        if (w.length() > 10) {
            return false;
        }
        if (!containsVowel(w)) {
            return false;
        }
        if (BLOCKLIST.contains(w)) {
            return false;
        }
        if (looksLikePluralOrInflectedForm(w)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if word is plural
     * 
     * @param w The lowercase word to analyze.
     * @return A bool, true if it appears in singular form
     */
    private static boolean looksLikePluralOrInflectedForm(String w) {
        int len = w.length();
        if (len >= 4 && w.endsWith("ses")) {
            return true;
        }
        if (len >= 5 && w.endsWith("ies")) {
            return true;
        }
        if (len >= 5 && (w.endsWith("xes") || w.endsWith("ches") || w.endsWith("shes") || w.endsWith("oes"))) {
            return true;
        }
        if (len >= 5 && w.endsWith("ves") && !w.endsWith("ives")) {
            return true;
        }
        if (len >= 4 && w.endsWith("oys")) {
            return true;
        }
        if (len >= 4 && w.endsWith("eys")) {
            return true;
        }
        if (len >= 4 && w.endsWith("eas")) {
            return true;
        }
        if (len >= 5 && w.endsWith("olas")) {
            return true;
        }
        if (len >= 5 && w.endsWith("acos")) {
            return true;
        }
        if (len >= 6 && w.endsWith("chos")) {
            return true;
        }
        if (len >= 4 && w.endsWith("ums")) {
            return true;
        }
        if (len >= 4 && w.endsWith("ays")) {
            if (!"always".equals(w) && !"says".equals(w)) {
                return true;
            }
        }
        if (len >= 5 && w.endsWith("es")) {
            if (!w.endsWith("ses") && !w.endsWith("ies") && !w.endsWith("oes")) {
                char beforeEs = w.charAt(w.length() - 3);
                if (!VOWELS.contains(beforeEs)) {
                    return true;
                }
            }
        }
        if (len >= 4 && w.endsWith("s")) {
            if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")
                    || w.endsWith("as") || w.endsWith("os")) {
                return false;
            }
            if (S_WORD_SINGULAR_WHITELIST.contains(w)) {
                return false;
            }
            char secondLast = w.charAt(len - 2);
            if (!VOWELS.contains(secondLast)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the string for the presence of at least one vowel
     * 
     * @param w The word to check
     * @return A bool, true if it has at least one vowel
     */
    private static boolean containsVowel(String w) {
        for (int i = 0; i < w.length(); i++) {
            if (VOWELS.contains(w.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
