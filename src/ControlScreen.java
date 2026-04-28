import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates the Parentental and Teacher control screen, allows for modification of game settings,
 * like number of starting lives and power-up distribution. 
 * 
 * @author Boyu Yang
 */

public class ControlScreen extends JPanel {

    private static final Path ACCOUNTS_FILE;
    private static final Path SESSIONS_FILE;

    static {
        Path dir = Paths.get("src");
        ACCOUNTS_FILE = dir.resolve("accounts.json");
        SESSIONS_FILE = dir.resolve("sessions.json");
    }

    private static final double BASE_W = 1920.0;
    private static final double BASE_H = 1080.0;

    private final Runnable onBack;
    private final Runnable onLogout;
    private final String loggedInUsername;

    private final JLabel titleLabel;
    private JTextField startingLivesField;
    private final List<JLabel> scaledBodyLabels = new ArrayList<>();
    private final List<JButton> scaledMenuButtons = new ArrayList<>();
    private JButton powerUpsToggleButton;
    private boolean powerUpsOn;

    /**
     * ControlScreen Constructor
     *
     * @param onBack   Callback to return to the main menu.
     * @param onLogout Callback to handle account logout (e.g., after password reset).
     * @param loggedInUsername The username of the current user.
     */
    public ControlScreen(Runnable onBack, Runnable onLogout, String loggedInUsername) {
        this.onBack = onBack;
        this.onLogout = onLogout;
        this.loggedInUsername = loggedInUsername != null ? loggedInUsername : "";

        setLayout(new BorderLayout(0, 40));
        setBorder(BorderFactory.createEmptyBorder(70, 40, 40, 40));
        setBackground(new Color(235, 235, 235));

        GameSettings gs = GameSettings.load();
        powerUpsOn = gs.isPowerUpsEnabled();

        this.titleLabel = build_TitleLabel();
        add(titleLabel, BorderLayout.NORTH);
        add(build_ControlsPanel(gs), BorderLayout.CENTER);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setOpaque(false);
        backPanel.add(build_BackButton());
        add(backPanel, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyHomeScaledFonts();
            }
        });
        SwingUtilities.invokeLater(this::applyHomeScaledFonts);
    }

    /** Text size scalling for text on buttons */
    private void applyHomeScaledFonts() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double scale = Math.min(w / BASE_W, h / BASE_H);
        Font buttonFont = new Font("SansSerif", Font.BOLD, Math.max(16, (int) (18 * scale)));

        for (JLabel label : scaledBodyLabels) {
            label.setFont(buttonFont);
        }
        if (startingLivesField != null) {
            startingLivesField.setFont(buttonFont);
        }
        for (JButton button : scaledMenuButtons) {
            button.setFont(buttonFont);
        }
    }

    /** Custom background graphic */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        double sx = w / BASE_W;
        double sy = h / BASE_H;

        g2.setColor(new Color(235, 235, 235));
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(215, 215, 215));

        fillPath(g2, sx, sy,
                1473, 474, 1441, 427, 1371, 457, 1248, 256, 1042, 355, 931, 281,
                968, 182, 1053, 145, 1070, 179, 1103, 167, 1107, 137, 1150, 139,
                1161, 86, 1090, 55, 911, 127, 816, 278, 778, 244, 693, 242,
                653, 286, 659, 335, 732, 379, 648, 439, 557, 365, 557, 314,
                519, 299, 502, 334, 451, 335, 446, 390, 509, 414, 595, 506,
                605, 491, 529, 406, 466, 377, 470, 351, 513, 355, 537, 326,
                538, 374, 650, 461, 771, 375, 678, 329, 672, 293, 703, 260,
                774, 263, 821, 306, 925, 142, 1088, 75, 1137, 96, 1135, 118,
                1090, 116, 1080, 155, 1063, 120, 953, 166, 909, 289, 1042, 376,
                1241, 280, 1360, 480, 1433, 451, 1432, 497);

        fillPath(g2, sx, sy,
                1311, 486, 1224, 345, 1044, 452, 1055, 483, 1068, 482,
                1073, 478, 1068, 459, 1217, 370, 1295, 493);

        fillPath(g2, sx, sy,
                704, 479, 709, 491, 714, 494, 794, 446,
                895, 492, 902, 490, 906, 476, 791, 425);

        fillPath(g2, sx, sy,
                993, 584, 1037, 683, 1256, 725, 1268, 812, 1319, 818, 1353, 657,
                1117, 597, 1107, 575, 1091, 581, 1104, 614, 1333, 674, 1304, 798,
                1287, 798, 1273, 708, 1052, 667, 1010, 580);

        g2.dispose();
    }

    /** Draws lines in custom graphic */
    private static void fillPath(Graphics2D g2, double sx, double sy, int... pts) {
        java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
        path.moveTo(pts[0] * sx, pts[1] * sy);
        for (int i = 2; i < pts.length; i += 2) {
            path.lineTo(pts[i] * sx, pts[i + 1] * sy);
        }
        path.closePath();
        g2.fill(path);
    }

    /**
     * Configures main title for control screen
     * 
     * @return A String
     */
    private JLabel build_TitleLabel() {
        JLabel title = new JLabel("PARENT/TEACHER CONTROLS", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.PLAIN, 42));
        title.setForeground(Color.BLACK);
        return title;
    }

    /**
     * Assembles the settings pannel
     * 
     * @param gs The current {@link GameSettings} to populate the UI fields.
     * @return A {@link javax.swing.JPanel} containing the setting controls.
     */
    private JPanel build_ControlsPanel(GameSettings gs) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Top row: lives input box on the left, power-up toggle + apply buttons on the
        // right
        startingLivesField = createStyledTextField(String.valueOf(gs.getStartingLives()));

        powerUpsToggleButton = createMenuButton(powerUpsLabel());
        powerUpsToggleButton.addActionListener(e -> {
            powerUpsOn = !powerUpsOn;
            powerUpsToggleButton.setText(powerUpsLabel());
        });

        JButton applySettings = createMenuButton("APPLY GAME SETTINGS");
        applySettings.addActionListener(e -> applyGameSettings());

        // Left side: label + input field
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
        leftSide.setOpaque(false);
        leftSide.add(make_Label("Starting lives (each new game)"));
        leftSide.add(Box.createRigidArea(new Dimension(0, 6)));
        leftSide.add(startingLivesField);

        // Right side: power-up toggle + apply buttons stacked vertically
        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setOpaque(false);
        powerUpsToggleButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        applySettings.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightSide.add(powerUpsToggleButton);
        rightSide.add(Box.createRigidArea(new Dimension(0, 10)));
        rightSide.add(applySettings);

        JPanel topRow = new JPanel(new BorderLayout(20, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(900, 120));
        topRow.add(leftSide, BorderLayout.WEST);
        topRow.add(rightSide, BorderLayout.EAST);
        panel.add(topRow);
        panel.add(Box.createRigidArea(new Dimension(0, 52)));

        panel.add(make_Row("Reset session history (scores and sessions)", "RESET", e -> handleResetSession()));
        panel.add(Box.createRigidArea(new Dimension(0, 32)));

        panel.add(make_Row("Reset an account password", "SELECT & RESET", e -> handleResetPassword()));
        panel.add(Box.createRigidArea(new Dimension(0, 32)));

        panel.add(make_Row("Delete an account", "SELECT & DELETE", e -> handleDeleteAccount()));
        return panel;
    }

    private String powerUpsLabel() {
        return powerUpsOn ? "POWER-UPS: ON" : "POWER-UPS: OFF";
    }

    /**
     * Text fomating
     * 
     * @param text The text to display on the label.
     * @return A {@Link javax.swing.JLabel} with formating
     */
    private JLabel make_Label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        scaledBodyLabels.add(label);
        return label;
    }

    /**
     * Input Field formating
     * 
     * @param text The inital text to be displayed in the field
     * @return A {@link javax.swing.JTextField} with formating
     */
    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text, 8);
        field.setFont(new Font("SansSerif", Font.BOLD, 18));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setBorder(new LineBorder(Color.BLACK, 4));
        field.setMaximumSize(new Dimension(400, 48));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    /**
     * Row formating
     * 
     * @param text The row descriptor
     * @param buttonText The button text
     * @param l The button effect
     * @return A {@link javax.swing.JPanel} with formating
     */
    private JPanel make_Row(String text, String buttonText, java.awt.event.ActionListener l) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(900, 58));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        scaledBodyLabels.add(label);
        row.add(label, BorderLayout.WEST);

        JButton btn = createMenuButton(buttonText);
        btn.addActionListener(l);
        row.add(btn, BorderLayout.EAST);

        return row;
    }

    /** 
     * Creates back button 
     * 
     * @return A {@link javax.swing.JButton}
    */
    private JButton build_BackButton() {
        JButton back = createMenuButton("BACK TO MENU");
        back.addActionListener(e -> onBack.run());
        return back;
    }

    /** 
     * Button formating
     * 
     * @param text Button text
     * @return A {@link javax.swing.JButton} with formating
    */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                new javax.swing.border.EmptyBorder(8, 12, 8, 12)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        scaledMenuButtons.add(button);
        return button;
    }

    /** Applies game settings */
    private void applyGameSettings() {
        try {
            int start = Integer.parseInt(startingLivesField.getText().trim());
            if (start < 1 || start > 99) {
                JOptionPane.showMessageDialog(this, "Starting lives must be between 1 and 99.");
                return;
            }
            GameSettings s = GameSettings.load();
            s.setStartingLives(start);
            s.setPowerUpsEnabled(powerUpsOn);
            s.save();
            JOptionPane.showMessageDialog(this, "Game settings saved.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number for starting lives.");
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not save settings.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Manages session history */
    private void handleResetSession() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete all session history?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Files.deleteIfExists(SESSIONS_FILE);
                JOptionPane.showMessageDialog(this, "Session data has been reset.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error resetting session data.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Resets user's password */
    private void handleResetPassword() {
        try {
            List<Account> accounts = loadAccounts();
            if (accounts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No accounts found.");
                return;
            }

            String[] usernames = accounts.stream().map(a -> a.username).toArray(String[]::new);
            String selectedUser = (String) JOptionPane.showInputDialog(
                    this,
                    "Select account to reset password:",
                    "Reset Password",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    usernames,
                    usernames[0]);

            if (selectedUser != null) {
                JPanel panel = new JPanel(new GridLayout(2, 1));
                panel.add(new JLabel("Enter new password for " + selectedUser + ":"));
                JPasswordField passwordField = new JPasswordField(20);
                panel.add(passwordField);

                int result = JOptionPane.showConfirmDialog(this, panel, "New Password", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    String newPassword = new String(passwordField.getPassword());
                    if (newPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Password cannot be empty.");
                        return;
                    }

                    for (Account acc : accounts) {
                        if (acc.username.equals(selectedUser)) {
                            acc.password = newPassword;
                            break;
                        }
                    }
                    saveAccounts(accounts);

                    boolean sameAsLoggedIn = selectedUser.equalsIgnoreCase(loggedInUsername);
                    if (sameAsLoggedIn) {
                        JOptionPane.showMessageDialog(this,
                                "Password changed. Please log in again with your new password.");
                        SwingUtilities.invokeLater(onLogout);
                    } else {
                        JOptionPane.showMessageDialog(this, "Password reset successfully.");
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading accounts.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Deletes user's account */
    private void handleDeleteAccount() {
        try {
            List<Account> accounts = loadAccounts();
            if (accounts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No accounts found.");
                return;
            }

            String[] usernames = accounts.stream().map(a -> a.username).toArray(String[]::new);
            String selectedUser = (String) JOptionPane.showInputDialog(
                    this,
                    "Select account to delete:",
                    "Delete Account",
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    usernames,
                    usernames[0]);

            if (selectedUser != null) {
                if (selectedUser.equalsIgnoreCase(loggedInUsername)) {
                    JOptionPane.showMessageDialog(this,
                            "This account is currently logged in and cannot be deleted.");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you extremely sure you want to delete account: " + selectedUser
                                + "?\nThis will also remove their session history.",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    accounts.removeIf(a -> a.username.equals(selectedUser));
                    saveAccounts(accounts);
                    removeUserFromStats(selectedUser);
                    JOptionPane.showMessageDialog(this,
                            "Account " + selectedUser + " and their statistics deleted successfully.");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class Account {
        String username;
        String password;
        boolean parentTeacher;

        Account(String username, String password, boolean parentTeacher) {
            this.username = username;
            this.password = password;
            this.parentTeacher = parentTeacher;
        }
    }

    /** Gets all accounts from save file */
    private List<Account> loadAccounts() throws IOException {
        List<Account> accounts = new ArrayList<>();
        if (!Files.exists(ACCOUNTS_FILE)) {
            return accounts;
        }

        String json = Files.readString(ACCOUNTS_FILE, StandardCharsets.UTF_8).trim();
        if (json.isEmpty() || json.equals("[]")) {
            return accounts;
        }

        Pattern objectPattern = Pattern.compile(
                "\\{\\s*\"username\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*"
                        + "\"password\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*"
                        + "\"parentTeacher\"\\s*:\\s*(true|false)\\s*\\}");

        Matcher matcher = objectPattern.matcher(json);
        while (matcher.find()) {
            String username = unescapeJson(matcher.group(1));
            String password = unescapeJson(matcher.group(2));
            boolean parentTeacher = Boolean.parseBoolean(matcher.group(3));
            accounts.add(new Account(username, password, parentTeacher));
        }

        return accounts;
    }

    /**
     * Writes new account info to save file
     * 
     * @param accounts List of accounts
     */
    private void saveAccounts(List<Account> accounts) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            json.append("  {\n");
            json.append("    \"username\": \"").append(escapeJson(account.username)).append("\",\n");
            json.append("    \"password\": \"").append(escapeJson(account.password)).append("\",\n");
            json.append("    \"parentTeacher\": ").append(account.parentTeacher).append("\n");
            json.append("  }");

            if (i < accounts.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");

        Files.writeString(ACCOUNTS_FILE, json.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Removes user for sessions save file
     * 
     * @param username The usernam of the user to be removed
     */
    private void removeUserFromStats(String username) throws IOException {
        if (Files.exists(SESSIONS_FILE)) {
            String content = Files.readString(SESSIONS_FILE, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile(
                    "\\{\\s*\"username\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*"
                            + "\"difficulty\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*"
                            + "\"score\"\\s*:\\s*(\\d+)\\s*,\\s*"
                            + "\"accuracy\"\\s*:\\s*([0-9.]+)\\s*,\\s*"
                            + "\"wpm\"\\s*:\\s*(\\d+)\\s*,\\s*"
                            + "\"date\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*\\}");
            Matcher matcher = pattern.matcher(content);
            StringBuilder sb = new StringBuilder("[\n");
            boolean first = true;
            while (matcher.find()) {
                String u = unescapeJson(matcher.group(1));
                if (!u.equals(username)) {
                    if (!first) {
                        sb.append(",\n");
                    }
                    sb.append("  {\n");
                    sb.append("    \"username\": \"").append(escapeJson(u)).append("\",\n");
                    sb.append("    \"difficulty\": \"").append(matcher.group(2)).append("\",\n");
                    sb.append("    \"score\": ").append(matcher.group(3)).append(",\n");
                    sb.append("    \"accuracy\": ").append(matcher.group(4)).append(",\n");
                    sb.append("    \"wpm\": ").append(matcher.group(5)).append(",\n");
                    sb.append("    \"date\": \"").append(matcher.group(6)).append("\"\n");
                    sb.append("  }");
                    first = false;
                }
            }
            sb.append("\n]\n");
            Files.writeString(SESSIONS_FILE, sb.toString(), StandardCharsets.UTF_8);
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
