import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Primary entry point for the game, manages loggin in and account creation
 * 
 * @author Daniel Park
 */

public class Run_MainMenuScreen extends JPanel {

    private static final Dimension MIN_FRAME_SIZE = new Dimension(800, 450);

    private static final double BASE_W = 1920.0;
    private static final double BASE_H = 1080.0;
    private static final Path ACCOUNTS_FILE;

    static {
        Path dir = Paths.get("src");
        ACCOUNTS_FILE = dir.resolve("accounts.json");
    }

    private float promptAlpha = 1.0f;
    private float fadeStep = -0.03f;

    private boolean showLoginMenu = false;
    private boolean showRegisterMenu = false;
    private static boolean hasShownSplash = false;

    private final JLabel usernameLabel;
    private final JLabel passwordLabel;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JButton exitButton;
    private final JLabel loginMessageLabel;

    private final JLabel registerUsernameLabel;
    private final JLabel registerPasswordLabel;
    private final JLabel confirmPasswordLabel;
    private final JTextField registerUsernameField;
    private final JPasswordField registerPasswordField;
    private final JPasswordField confirmPasswordField;
    private final JCheckBox parentGuardianCheckBox;
    private final JButton createAccountButton;
    private final JButton backButton;
    private final JLabel registerMessageLabel;

    /** Run_MainMenuScreen Constructor */
    public Run_MainMenuScreen() {
        setBackground(new Color(235, 235, 235));
        setFocusable(true);
        setLayout(null);

        usernameLabel = new JLabel("USERNAME");
        usernameLabel.setVisible(false);

        passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setVisible(false);

        usernameField = new JTextField();
        usernameField.setVisible(false);

        passwordField = new JPasswordField();
        passwordField.setVisible(false);

        loginButton = createMenuButton("LOGIN");
        loginButton.setVisible(false);

        registerButton = createMenuButton("REGISTER");
        registerButton.setVisible(false);

        exitButton = createMenuButton("EXIT");
        exitButton.setVisible(false);

        loginMessageLabel = new JLabel("", SwingConstants.CENTER);
        loginMessageLabel.setForeground(Color.RED);
        loginMessageLabel.setVisible(false);

        registerUsernameLabel = new JLabel("USERNAME");
        registerUsernameLabel.setVisible(false);

        registerPasswordLabel = new JLabel("PASSWORD");
        registerPasswordLabel.setVisible(false);

        confirmPasswordLabel = new JLabel("CONFIRM PASSWORD");
        confirmPasswordLabel.setVisible(false);

        registerUsernameField = new JTextField();
        registerUsernameField.setVisible(false);

        registerPasswordField = new JPasswordField();
        registerPasswordField.setVisible(false);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setVisible(false);

        parentGuardianCheckBox = new JCheckBox("I am a parent/teacher");
        parentGuardianCheckBox.setOpaque(false);
        parentGuardianCheckBox.setFocusable(false);
        parentGuardianCheckBox.setVisible(false);

        createAccountButton = createMenuButton("CREATE ACCOUNT");
        createAccountButton.setVisible(false);

        backButton = createMenuButton("BACK");
        backButton.setVisible(false);

        registerMessageLabel = new JLabel("", SwingConstants.CENTER);
        registerMessageLabel.setForeground(Color.RED);
        registerMessageLabel.setVisible(false);

        add(usernameLabel);
        add(passwordLabel);
        add(usernameField);
        add(passwordField);
        add(loginButton);
        add(registerButton);
        add(exitButton);
        add(loginMessageLabel);

        add(registerUsernameLabel);
        add(registerPasswordLabel);
        add(confirmPasswordLabel);
        add(registerUsernameField);
        add(registerPasswordField);
        add(confirmPasswordField);
        add(parentGuardianCheckBox);
        add(createAccountButton);
        add(backButton);
        add(registerMessageLabel);

        Timer fadeTimer = new Timer(40, e -> {
            if (!showLoginMenu && !showRegisterMenu) {
                promptAlpha += fadeStep;

                if (promptAlpha <= 0.0f) {
                    promptAlpha = 0.0f;
                    fadeStep = 0.03f;
                } else if (promptAlpha >= 1.0f) {
                    promptAlpha = 1.0f;
                    fadeStep = -0.03f;
                }

                repaint();
            }
        });
        fadeTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                openLoginFromSplash();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLoginFromSplash();
            }
        });

        registerButton.addActionListener(e -> {
            hideLoginComponents();
            showLoginMenu = false;
            showRegisterMenu = true;
            showRegisterComponents();
        });

        backButton.addActionListener(e -> {
            hideRegisterComponents();
            showRegisterMenu = false;
            showLoginMenu = true;
            showLoginComponents();
        });

        createAccountButton.addActionListener(e -> handleRegister());
        loginButton.addActionListener(e -> handleLogin());
        exitButton.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.dispose();
            }
            System.exit(0);
        });

        if (hasShownSplash) {
            showLoginMenu = true;
            showLoginComponents();
        }
    }

    /** Leaves splash screen and moves to login screen */
    private void openLoginFromSplash() {
        if (!showLoginMenu && !showRegisterMenu) {
            showLoginMenu = true;
            hasShownSplash = true;
            showLoginComponents();
        }
    }

    /** Makes login components visible and focuses game window */
    private void showLoginComponents() {
        usernameLabel.setVisible(true);
        passwordLabel.setVisible(true);
        usernameField.setVisible(true);
        passwordField.setVisible(true);
        loginButton.setVisible(true);
        registerButton.setVisible(true);
        loginMessageLabel.setVisible(true);
        loginMessageLabel.setText("");

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    /** Hides login components */
    private void hideLoginComponents() {
        usernameLabel.setVisible(false);
        passwordLabel.setVisible(false);
        usernameField.setVisible(false);
        passwordField.setVisible(false);
        loginButton.setVisible(false);
        registerButton.setVisible(false);
        loginMessageLabel.setVisible(false);
    }

    /** Makes register components visible and focuses game window */
    private void showRegisterComponents() {
        registerUsernameLabel.setVisible(true);
        registerPasswordLabel.setVisible(true);
        confirmPasswordLabel.setVisible(true);
        registerUsernameField.setVisible(true);
        registerPasswordField.setVisible(true);
        confirmPasswordField.setVisible(true);
        parentGuardianCheckBox.setVisible(true);
        createAccountButton.setVisible(true);
        backButton.setVisible(true);
        registerMessageLabel.setVisible(true);
        registerMessageLabel.setForeground(Color.RED);
        registerMessageLabel.setText("");
        parentGuardianCheckBox.setSelected(false);

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> registerUsernameField.requestFocusInWindow());
    }

    /** Hides register components */
    private void hideRegisterComponents() {
        registerUsernameLabel.setVisible(false);
        registerPasswordLabel.setVisible(false);
        confirmPasswordLabel.setVisible(false);
        registerUsernameField.setVisible(false);
        registerPasswordField.setVisible(false);
        confirmPasswordField.setVisible(false);
        parentGuardianCheckBox.setVisible(false);
        createAccountButton.setVisible(false);
        backButton.setVisible(false);
        registerMessageLabel.setVisible(false);
    }

    /** Validates regristration details */
    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        boolean isParentTeacher = parentGuardianCheckBox.isSelected();

        registerMessageLabel.setForeground(Color.RED);

        if (username.isEmpty()) {
            registerMessageLabel.setText("Username cannot be empty.");
            return;
        }

        if (password.isEmpty()) {
            registerMessageLabel.setText("Password cannot be empty.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            registerMessageLabel.setText("Passwords do not match. Please retype.");
            registerPasswordField.setText("");
            confirmPasswordField.setText("");
            SwingUtilities.invokeLater(() -> registerPasswordField.requestFocusInWindow());
            return;
        }

        try {
            ensureAccountsFileExists();
            List<Account> accounts = loadAccounts();

            for (Account account : accounts) {
                if (account.username.equalsIgnoreCase(username)) {
                    registerMessageLabel.setForeground(Color.RED);
                    registerMessageLabel.setText("That username is already taken.");
                    return;
                }
            }

            accounts.add(new Account(username, password, isParentTeacher));
            saveAccounts(accounts);

            registerMessageLabel.setForeground(new Color(0, 128, 0));
            registerMessageLabel.setText("Account created. Please sign in.");

            Timer timer = new Timer(1200, e -> {
                clearRegisterFields();
                hideRegisterComponents();
                showRegisterMenu = false;
                showLoginMenu = true;
                showLoginComponents();
                usernameField.setText(username);
                passwordField.setText("");
                loginMessageLabel.setForeground(new Color(0, 128, 0));
                loginMessageLabel.setText("Please sign in with your new account.");
                SwingUtilities.invokeLater(() -> passwordField.requestFocusInWindow());
            });
            timer.setRepeats(false);
            timer.start();

        } catch (IOException ex) {
            registerMessageLabel.setForeground(Color.RED);
            registerMessageLabel.setText("Could not save account.");
            ex.printStackTrace();
        }
    }

    /** Validates login, leads to home screen on success */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        loginMessageLabel.setForeground(Color.RED);

        if (username.isEmpty()) {
            loginMessageLabel.setText("Enter your username.");
            return;
        }

        if (password.isEmpty()) {
            loginMessageLabel.setText("Enter your password.");
            return;
        }

        try {
            ensureAccountsFileExists();
            List<Account> accounts = loadAccounts();

            for (Account account : accounts) {
                if (account.username.equalsIgnoreCase(username) && account.password.equals(password)) {
                    loginMessageLabel.setForeground(new Color(0, 128, 0));
                    loginMessageLabel.setText("Log in successful.");

                    Timer timer = new Timer(700, e -> openHomeScreen(account.username, account.parentTeacher));
                    timer.setRepeats(false);
                    timer.start();
                    return;
                }
            }

            loginMessageLabel.setText("Invalid username or password.");

        } catch (IOException ex) {
            loginMessageLabel.setText("Could not load accounts.");
            ex.printStackTrace();
        }
    }

    /**
     * Opens the home screen
     * 
     * @param username The username of the user
     * @param isParentTeacher A bool, true if is Parent or Teacher
     */
    private void openHomeScreen(String username, boolean isParentTeacher) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }

        SwingUtilities.invokeLater(() -> {
            Home home = new Home(username, isParentTeacher);
            home.setVisible(true);
        });
    }

    /** Removes text from register fields */
    private void clearRegisterFields() {
        registerUsernameField.setText("");
        registerPasswordField.setText("");
        confirmPasswordField.setText("");
        parentGuardianCheckBox.setSelected(false);
        registerMessageLabel.setText("");
    }

    /**
     * Creates the buttons
     * 
     * @param text The button text
     * @return A {@link javax.swing.JButton} formatted
     */
    private static JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setBorder(new LineBorder(Color.BLACK, 4));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Calculates the total width of a button
     * 
     * @param button The {@link javax.swing.JButton} to be measured
     * @param extraPadding Additional horizontal pixels to add to the total width
     * @return The calculated width of the button
     */
    private int getButtonWidth(JButton button, int extraPadding) {
        FontMetrics fm = button.getFontMetrics(button.getFont());
        Insets insets = button.getInsets();
        return fm.stringWidth(button.getText()) + insets.left + insets.right + extraPadding;
    }

    /** Requests input focus for window */
    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    /** Dynamic layout scaling for different window resolutions */
    @Override
    public void doLayout() {
        super.doLayout();

        int w = getWidth();
        int h = getHeight();

        double sx = w / BASE_W;
        double sy = h / BASE_H;

        float scale = (float) Math.min(sx, sy);

        Font labelFont = new Font("SansSerif", Font.BOLD, Math.max(18, (int) (22 * scale)));
        Font fieldFont = new Font("SansSerif", Font.PLAIN, Math.max(18, (int) (22 * scale)));
        Font menuButtonFont = new Font("SansSerif", Font.BOLD, Math.max(16, (int) (18 * scale)));
        Font messageFont = new Font("SansSerif", Font.BOLD, Math.max(14, (int) (18 * scale)));
        Font checkBoxFont = new Font("SansSerif", Font.PLAIN, Math.max(16, (int) (19 * scale)));

        usernameLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        registerUsernameLabel.setFont(labelFont);
        registerPasswordLabel.setFont(labelFont);
        confirmPasswordLabel.setFont(labelFont);

        usernameField.setFont(fieldFont);
        passwordField.setFont(fieldFont);
        registerUsernameField.setFont(fieldFont);
        registerPasswordField.setFont(fieldFont);
        confirmPasswordField.setFont(fieldFont);

        loginButton.setFont(menuButtonFont);
        registerButton.setFont(menuButtonFont);
        exitButton.setFont(menuButtonFont);
        createAccountButton.setFont(menuButtonFont);
        backButton.setFont(menuButtonFont);
        loginMessageLabel.setFont(messageFont);
        registerMessageLabel.setFont(messageFont);
        parentGuardianCheckBox.setFont(checkBoxFont);

        loginButton.setMargin(new Insets(8, 18, 8, 18));
        registerButton.setMargin(new Insets(8, 18, 8, 18));
        exitButton.setMargin(new Insets(8, 18, 8, 18));
        createAccountButton.setMargin(new Insets(8, 22, 8, 22));
        backButton.setMargin(new Insets(8, 18, 8, 18));

        FontMetrics labelMetrics = getFontMetrics(labelFont);

        int horizontalPadding = Math.max(28, (int) (36 * sx));
        int labelW = Math.max(
                Math.max(labelMetrics.stringWidth("USERNAME"), labelMetrics.stringWidth("PASSWORD")),
                labelMetrics.stringWidth("CONFIRM PASSWORD")) + horizontalPadding;

        int fieldW = Math.max(300, (int) (360 * sx));
        int fieldH = Math.max(42, (int) (50 * sy));
        int buttonH = Math.max(48, (int) (54 * sy));
        int rowGap = Math.max(12, (int) (18 * sy));
        int buttonGap = Math.max(12, (int) (20 * sx));
        int labelFieldGap = Math.max(12, (int) (20 * sx));
        int checkBoxH = Math.max(32, (int) (38 * sy));

        int loginButtonW = getButtonWidth(loginButton, Math.max(24, (int) (24 * sx)));
        int registerButtonW = getButtonWidth(registerButton, Math.max(24, (int) (24 * sx)));
        int exitButtonW = getButtonWidth(exitButton, Math.max(24, (int) (24 * sx)));
        int createAccountButtonW = getButtonWidth(createAccountButton, Math.max(28, (int) (28 * sx)));
        int backButtonW = getButtonWidth(backButton, Math.max(24, (int) (24 * sx)));

        int formTotalW = labelW + labelFieldGap + fieldW;
        int startX = (w - formTotalW) / 2;
        int startY = (int) (700 * sy);

        usernameLabel.setBounds(startX, startY, labelW, fieldH);
        usernameField.setBounds(startX + labelW + labelFieldGap, startY, fieldW, fieldH);

        passwordLabel.setBounds(startX, startY + fieldH + rowGap, labelW, fieldH);
        passwordField.setBounds(startX + labelW + labelFieldGap, startY + fieldH + rowGap, fieldW, fieldH);

        int loginButtonY = startY + (fieldH + rowGap) * 2;
        int loginButtonStartX = startX + labelW + labelFieldGap;

        loginButton.setBounds(loginButtonStartX, loginButtonY, loginButtonW, buttonH);
        registerButton.setBounds(loginButtonStartX + loginButtonW + buttonGap, loginButtonY, registerButtonW, buttonH);

        int loginMessageY = loginButtonY + buttonH + 6;
        int loginButtonsTotalW = loginButtonW + buttonGap + registerButtonW + buttonGap + exitButtonW;
        loginMessageLabel.setBounds(loginButtonStartX, loginMessageY, loginButtonsTotalW, 28);

        int registerStartY = (int) (620 * sy);

        registerUsernameLabel.setBounds(startX, registerStartY, labelW, fieldH);
        registerUsernameField.setBounds(startX + labelW + labelFieldGap, registerStartY, fieldW, fieldH);

        registerPasswordLabel.setBounds(startX, registerStartY + fieldH + rowGap, labelW, fieldH);
        registerPasswordField.setBounds(startX + labelW + labelFieldGap, registerStartY + fieldH + rowGap, fieldW,
                fieldH);

        int confirmY = registerStartY + (fieldH + rowGap) * 2;
        confirmPasswordLabel.setBounds(startX, confirmY, labelW, fieldH);
        confirmPasswordField.setBounds(startX + labelW + labelFieldGap, confirmY, fieldW, fieldH);

        int checkBoxY = confirmY + fieldH + rowGap;
        parentGuardianCheckBox.setBounds(startX + labelW + labelFieldGap, checkBoxY, fieldW, checkBoxH);

        int registerButtonStartX = startX + labelW + labelFieldGap;
        int registerButtonY = checkBoxY + checkBoxH + 8;

        createAccountButton.setBounds(registerButtonStartX, registerButtonY, createAccountButtonW, buttonH);
        backButton.setBounds(registerButtonStartX + createAccountButtonW + buttonGap, registerButtonY, backButtonW,
                buttonH);

        int messageY = registerButtonY + buttonH + 6;
        int buttonsTotalW = createAccountButtonW + buttonGap + backButtonW;
        registerMessageLabel.setBounds(registerButtonStartX, messageY, buttonsTotalW, 28);

        int loginExitX = loginButtonStartX + loginButtonW + buttonGap + registerButtonW + buttonGap;

        if (showLoginMenu) {
            exitButton.setBounds(loginExitX, loginButtonY, exitButtonW, buttonH);
            exitButton.setVisible(true);
        } else {
            exitButton.setVisible(false);
        }
    }

    /** Paints the objects to the screen */
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

        g2.setColor(Color.BLACK);

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

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setFont(new Font("SansSerif", Font.PLAIN, (int) (85 * Math.min(sx, sy))));
        String title = "DEATH BY SPELL CHECK";
        FontMetrics fmTitle = g2.getFontMetrics();
        int titleX = (w - fmTitle.stringWidth(title)) / 2;
        int titleY = (int) (570 * sy);
        drawOutlinedString(g2, title, titleX, titleY, Color.BLACK, Color.WHITE, 2);

        if (!showLoginMenu && !showRegisterMenu) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, promptAlpha));
            g2.setFont(new Font("SansSerif", Font.PLAIN, (int) (42 * Math.min(sx, sy))));
            String prompt = "PRESS ANY KEY";
            FontMetrics fmPrompt = g2.getFontMetrics();
            int promptX = (w - fmPrompt.stringWidth(prompt)) / 2;
            int promptY = (int) (925 * sy);
            g2.drawString(prompt, promptX, promptY);
        }

        g2.dispose();
    }

    /**
     * Custom text style
     * 
     * @param g2 The Graphics2D context
     * @param text The string to draw
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param fill The inner color
     * @param outline The border color
     * @param strokeRadius The thickness of the outline
     */
    private static void drawOutlinedString(Graphics2D g2, String text, int x, int y, Color fill, Color outline,
            int strokeRadius) {
        g2.setColor(outline);
        for (int dx = -strokeRadius; dx <= strokeRadius; dx++) {
            for (int dy = -strokeRadius; dy <= strokeRadius; dy++) {
                if (dx != 0 || dy != 0) {
                    g2.drawString(text, x + dx, y + dy);
                }
            }
        }
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    /**
     * Fills in lines in the custom background graphic
     * 
     * @param g2 The Graphics2D context
     * @param sx The horizontal scale factor
     * @param sy The vertical scale factor
     * @param pts The array of (x, y) coordinate pairs
     */
    private void fillPath(Graphics2D g2, double sx, double sy, int... pts) {
        GeneralPath path = new GeneralPath();
        path.moveTo(pts[0] * sx, pts[1] * sy);
        for (int i = 2; i < pts.length; i += 2) {
            path.lineTo(pts[i] * sx, pts[i + 1] * sy);
        }
        path.closePath();
        g2.fill(path);
    }

    /** Set perferred window size */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1280, 720);
    }

    /** Checks that the local storage files can be found */
    private void ensureAccountsFileExists() throws IOException {
        if (!Files.exists(ACCOUNTS_FILE)) {
            Path parent = ACCOUNTS_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(ACCOUNTS_FILE, "[]", StandardCharsets.UTF_8);
        }
    }

    /** Classs to store user info */
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

    /**
     * Loads the user data from the accounts save file
     * 
     * @return A list of all accounts
     * @thorws IOException If the file cannot be read
     */
    private List<Account> loadAccounts() throws IOException {
        ensureAccountsFileExists();

        List<Account> accounts = new ArrayList<>();
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
     * Saves the user data to the account save file
     * 
     * @return A list of all accounts to be saved
     * @throws IOException If the file cannot be read
     */
    private void saveAccounts(List<Account> accounts) throws IOException {
        ensureAccountsFileExists();

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

    /** Text Formating */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Text Formating */
    private static String unescapeJson(String text) {
        return text
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    /** Opens main menu */
    public static void openMainMenuWindow() {
        hasShownSplash = false;
        SwingUtilities.invokeLater(Run_MainMenuScreen::createAndShowMainMenuFrame);
    }

    /** Creates the main menu screen */
    private static void createAndShowMainMenuFrame() {
        JFrame frame = new JFrame("Death by Spell Check");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(MIN_FRAME_SIZE);
        frame.setContentPane(new Run_MainMenuScreen());
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");
        frame.getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Fullscreen.toggle(frame);
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Run_MainMenuScreen::createAndShowMainMenuFrame);
    }
}