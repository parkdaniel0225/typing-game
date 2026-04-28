import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to manage the settings for the game
 * 
 * @author Boyu Yang
 * @author Daniel Park
 */

public final class GameSettings {

    private static final Path FILE = Paths.get("game_settings.json");

    private static final Pattern BOOL_PATTERN = Pattern.compile("\"powerUpsEnabled\"\\s*:\\s*(true|false)");

    private int startingLives = 3;
    private int maxExtraLives = 2;
    private boolean powerUpsEnabled = true;

    /** Gets starting live */
    public int getStartingLives() {
        return startingLives;
    }

    /**
     * Sets starting lives
     * 
     * @param startingLives The number of starting lives
     */
    public void setStartingLives(int startingLives) {
        this.startingLives = startingLives;
    }

    /** Gets max extra lives */
    public int getMaxExtraLives() {
        return maxExtraLives;
    }

    /**
     * Sets maximum extra lives
     * 
     * @param maxExtraLives The maximum number of extra lives
     */
    public void setMaxExtraLives(int maxExtraLives) {
        this.maxExtraLives = maxExtraLives;
    }

    /** Gets max total lives */
    public int getMaxTotalLives() {
        return Math.min(99, startingLives + maxExtraLives);
    }

    /** Gets power-up usability */
    public boolean isPowerUpsEnabled() {
        return powerUpsEnabled;
    }

    /**
     * Sets power-up usability
     * 
     * @param powerUpsEnabled A bool, true is power-ups are allowed
    */
    public void setPowerUpsEnabled(boolean powerUpsEnabled) {
        this.powerUpsEnabled = powerUpsEnabled;
    }

    /** Loads game settings for save file */
    public static GameSettings load() {
        GameSettings s = new GameSettings();
        if (!Files.exists(FILE)) {
            return s;
        }
        try {
            String json = Files.readString(FILE, StandardCharsets.UTF_8);
            Matcher startM = Pattern.compile("\"startingLives\"\\s*:\\s*(\\d+)").matcher(json);
            if (startM.find()) {
                s.startingLives = Integer.parseInt(startM.group(1));
            }
            Matcher extraM = Pattern.compile("\"maxExtraLives\"\\s*:\\s*(\\d+)").matcher(json);
            if (extraM.find()) {
                s.maxExtraLives = Integer.parseInt(extraM.group(1));
            } else {
                Matcher legacyMax = Pattern.compile("\"maxTotalLives\"\\s*:\\s*(\\d+)").matcher(json);
                if (legacyMax.find()) {
                    int total = Integer.parseInt(legacyMax.group(1));
                    s.maxExtraLives = Math.max(0, total - s.startingLives);
                }
            }
            Matcher boolM = BOOL_PATTERN.matcher(json);
            if (boolM.find()) {
                s.powerUpsEnabled = Boolean.parseBoolean(boolM.group(1));
            }
        } catch (IOException | NumberFormatException ignored) {
        }
        s.clamp();
        return s;
    }

    /** Saves game setting to save file */
    public void save() throws IOException {
        clamp();
        String json = "{\n"
                + "  \"startingLives\": " + startingLives + ",\n"
                + "  \"maxExtraLives\": " + maxExtraLives + ",\n"
                + "  \"powerUpsEnabled\": " + powerUpsEnabled + "\n"
                + "}\n";
        Files.writeString(FILE, json, StandardCharsets.UTF_8);
    }

    /** Fixes values if out of bounds */
    private void clamp() {
        if (startingLives < 1) {
            startingLives = 1;
        }
        if (startingLives > 99) {
            startingLives = 99;
        }
        if (maxExtraLives < 0) {
            maxExtraLives = 0;
        }
        if (startingLives + maxExtraLives > 99) {
            maxExtraLives = 99 - startingLives;
        }
    }
}
