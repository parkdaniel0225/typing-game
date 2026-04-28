import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * The main program for the game, manages navigation between screens
 * 
 * @author Daniel Park
 */

public class Home extends JFrame {

    private static final Dimension MIN_SIZE = new Dimension(800, 450);

    private final String username;
    private final boolean isParentTeacher;

    /**
     * Home Constructor
     * 
     * @param username A String with the username of the user
     * @param isParentTeacher A bool, true if the user is a parent/teacher
     */
    public Home(String username, boolean isParentTeacher) {
        this.username = username;
        this.isParentTeacher = isParentTeacher;

        setTitle("Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(MIN_SIZE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");
        getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Fullscreen.toggle(Home.this);
            }
        });

        HomePanel mainPanel = new HomePanel();
        mainPanel.setLayout(null);

        JLabel welcomeLabel = new OutlinedWelcomeLabel("Welcome, " + this.username);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 26));

        JButton startGameButton = createMenuButton("START GAME");
        JButton tutorialButton = createMenuButton("TUTORIAL");
        JButton highScoresButton = createMenuButton("HIGH SCORES");
        JButton playerInfoButton = createMenuButton("PLAYER INFORMATION");
        JButton parentTeacherButton = createMenuButton("PARENT/TEACHER CONTROLS");
        JButton switchUserButton = createMenuButton("SWITCH USER");
        JButton exitButton = createMenuButton("EXIT");

        if (!this.isParentTeacher) {
            parentTeacherButton.setEnabled(false);
            parentTeacherButton.setToolTipText("Only parent/teacher accounts can access this.");
            parentTeacherButton.setCursor(Cursor.getDefaultCursor());
        }

        startGameButton.addActionListener(e -> {
            JFrame gameFrame = new JFrame("Death by Spell Check");
            gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            gameFrame.setMinimumSize(MIN_SIZE);
            gameFrame.setSize(getWidth(), getHeight());
            gameFrame.setLocationRelativeTo(this);
            setVisible(false);
            gameFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent ev) {
                    setVisible(true);
                }
            });
            Runnable onBack = gameFrame::dispose;
            GameplayScreen gameplay = new GameplayScreen(onBack);
            gameplay.setData(this.username, new ArrayList<SessionRecord>());
            gameFrame.setContentPane(gameplay);
            gameFrame.setVisible(true);
        });

        tutorialButton.addActionListener(e -> {
            JFrame tutorialFrame = new JFrame("Tutorial");
            tutorialFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            tutorialFrame.setMinimumSize(MIN_SIZE);
            tutorialFrame.setSize(getWidth(), getHeight());
            tutorialFrame.setLocationRelativeTo(this);
            setVisible(false);
            tutorialFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent ev) {
                    setVisible(true);
                }
            });
            tutorialFrame.setContentPane(new GameTutorial());
            tutorialFrame.setVisible(true);
        });

        highScoresButton.addActionListener(e -> {
            JFrame highScoresFrame = new JFrame("High Scores");
            highScoresFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            highScoresFrame.setMinimumSize(MIN_SIZE);
            highScoresFrame.setSize(getWidth(), getHeight());
            highScoresFrame.setLocationRelativeTo(this);
            setVisible(false);
            highScoresFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent ev) {
                    setVisible(true);
                }
            });
            Runnable onBack = highScoresFrame::dispose;
            highScoresFrame.setContentPane(new HighScoresScreen(onBack));
            highScoresFrame.setVisible(true);
        });

        playerInfoButton.addActionListener(e -> {
            JFrame playerFrame = new JFrame("Player Information");
            playerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            playerFrame.setMinimumSize(MIN_SIZE);
            playerFrame.setSize(getWidth(), getHeight());
            playerFrame.setLocationRelativeTo(this);
            setVisible(false);
            playerFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent ev) {
                    setVisible(true);
                }
            });
            PlayerScreen playerScreen = new PlayerScreen(playerFrame);
            playerFrame.setContentPane(playerScreen);
            try {
                playerScreen.setData(this.username, SessionRecord.loadAllSessions());
            } catch (IOException ex) {
                playerScreen.setData(username, new ArrayList<SessionRecord>());
                ex.printStackTrace();
            }
            playerFrame.setVisible(true);
        });

        parentTeacherButton.addActionListener(e -> {
            if (this.isParentTeacher) {
                JFrame controlFrame = new JFrame("Parent/Teacher Controls");
                controlFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                controlFrame.setMinimumSize(MIN_SIZE);
                controlFrame.setSize(getWidth(), getHeight());
                controlFrame.setLocationRelativeTo(this);
                setVisible(false);
                controlFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent ev) {
                        setVisible(true);
                    }
                });
                Runnable onBack = controlFrame::dispose;
                Runnable onLogout = () -> {
                    controlFrame.dispose();
                    dispose();
                    Run_MainMenuScreen.openMainMenuWindow();
                };
                controlFrame.setContentPane(new ControlScreen(onBack, onLogout, this.username));
                controlFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Access denied.");
            }
        });

        switchUserButton.addActionListener(e -> {
            Object[] options = { "Yes", "Back" };
            int confirm = JOptionPane.showOptionDialog(
                    this,
                    "Are you sure you want to log out and switch users?",
                    "Confirm Switch User",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]); // Changed from options[1] to options[0]
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                Run_MainMenuScreen.openMainMenuWindow();
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        mainPanel.add(welcomeLabel);
        mainPanel.add(startGameButton);
        mainPanel.add(tutorialButton);
        mainPanel.add(playerInfoButton);
        mainPanel.add(highScoresButton);
        mainPanel.add(parentTeacherButton);
        mainPanel.add(switchUserButton);
        mainPanel.add(exitButton);

        setContentPane(mainPanel);

        SwingUtilities.invokeLater(() -> layoutComponents(
                mainPanel,
                welcomeLabel,
                startGameButton,
                tutorialButton,
                highScoresButton,
                playerInfoButton,
                parentTeacherButton,
                switchUserButton,
                exitButton));
    }

    /** Class for menu layout */
    private static final class HomeMenuLayout {
        int buttonW;
        int buttonH;
        int gap;
        int centerX;
        int startY;
        int welcomeLabelY;
        int welcomeLabelH;
    }

    /**
     * Calculates individual button positions based on window dimensions
     * 
     * @param w Current width of the window
     * @param h Current height of the window
     * @return A {@link HomeMenuLayout} with the positioning data
     */
    private static HomeMenuLayout computeHomeMenuLayout(int w, int h) {
        w = Math.max(1, w);
        h = Math.max(1, h);
        HomeMenuLayout L = new HomeMenuLayout();
        double scale = Math.min(w / 1920.0, h / 1080.0);
        L.welcomeLabelY = (int) (330 * scale);
        L.welcomeLabelH = 40;

        int welcomeBottom = L.welcomeLabelY + L.welcomeLabelH;
        L.buttonW = Math.max(300, Math.min((int) (580 * scale), w - 16));
        L.buttonH = Math.max(76, (int) (94 * scale));
        L.gap = Math.max(34, (int) (44 * scale));

        int paddingBelow = Math.max(8, (int) (32 * scale));
        int bottomMargin = Math.max(8, (int) Math.min(52 + 88 * scale, h * 0.14));

        int contentTop = welcomeBottom + paddingBelow;
        int maxStackH = h - contentTop - bottomMargin;
        if (maxStackH < 1) {
            maxStackH = 1;
        }

        int stackH = 7 * L.buttonH + 6 * L.gap;
        if (stackH > maxStackH) {
            double shrink = (double) maxStackH / stackH;
            L.buttonH = Math.max(44, (int) Math.floor(L.buttonH * shrink));
            L.gap = Math.max(10, (int) Math.floor(L.gap * shrink));
            stackH = 7 * L.buttonH + 6 * L.gap;
            int guard = 0;
            while (stackH > maxStackH && guard++ < 400 && (L.buttonH > 44 || L.gap > 10)) {
                if (L.gap > 10) {
                    L.gap--;
                } else if (L.buttonH > 44) {
                    L.buttonH--;
                } else {
                    break;
                }
                stackH = 7 * L.buttonH + 6 * L.gap;
            }
        }

        while (stackH > maxStackH && (bottomMargin > 8 || paddingBelow > 4)) {
            if (bottomMargin > 8) {
                bottomMargin = Math.max(8, bottomMargin - 4);
            } else {
                paddingBelow = Math.max(4, paddingBelow - 4);
            }
            contentTop = welcomeBottom + paddingBelow;
            maxStackH = Math.max(1, h - contentTop - bottomMargin);
        }

        stackH = 7 * L.buttonH + 6 * L.gap;
        while (stackH > maxStackH && (L.buttonH > 42 || L.gap > 10)) {
            if (L.gap > 10) {
                L.gap--;
            } else if (L.buttonH > 42) {
                L.buttonH--;
            } else {
                break;
            }
            stackH = 7 * L.buttonH + 6 * L.gap;
        }

        contentTop = welcomeBottom + paddingBelow;
        maxStackH = Math.max(1, h - contentTop - bottomMargin);
        stackH = 7 * L.buttonH + 6 * L.gap;

        int startY = contentTop + Math.max(0, (maxStackH - stackH) / 2);
        startY = Math.min(startY, h - bottomMargin - stackH);
        L.startY = Math.max(0, startY);
        if (L.startY + stackH > h - 8) {
            L.startY = Math.max(0, h - 8 - stackH);
        }

        L.centerX = Math.max(0, (w - L.buttonW) / 2);
        return L;
    }

    /** Sets bounds of each menu button based on computed layout */
    private void layoutComponents(
            JPanel panel,
            JLabel welcomeLabel,
            JButton startGameButton,
            JButton tutorialButton,
            JButton highScoresButton,
            JButton playerInfoButton,
            JButton parentTeacherButton,
            JButton switchUserButton,
            JButton exitButton) {
        int w = panel.getWidth();
        int h = panel.getHeight();

        HomeMenuLayout geo = computeHomeMenuLayout(w, h);

        welcomeLabel.setBounds(0, geo.welcomeLabelY, w, geo.welcomeLabelH);
        startGameButton.setBounds(geo.centerX, geo.startY, geo.buttonW, geo.buttonH);
        tutorialButton.setBounds(geo.centerX, geo.startY + (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
        highScoresButton.setBounds(geo.centerX, geo.startY + 3 * (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
        playerInfoButton.setBounds(geo.centerX, geo.startY + 2 * (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
        parentTeacherButton.setBounds(geo.centerX, geo.startY + 4 * (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
        switchUserButton.setBounds(geo.centerX, geo.startY + 5 * (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
        exitButton.setBounds(geo.centerX, geo.startY + 6 * (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
    }

    /** 
     * Creates a formatted menu buttons
     * 
     * @param text String with the button text
     * @return A {@link javax.swing.JButton} formatted
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 30));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                new EmptyBorder(12, 20, 12, 20)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }


    /**
     * Draws custom outlined text
     * 
     * @param g2 The Graphics2D context
     * @param text The string to draw
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param fill Main text colour
     * @param outline Outline colour
     * @param strokeRadius Thickness of the outline
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

    /** Class for custom outlined text */
    private static class OutlinedWelcomeLabel extends JLabel {
        OutlinedWelcomeLabel(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String t = getText();
            if (t == null) {
                t = "";
            }
            int tw = fm.stringWidth(t);
            int x = (getWidth() - tw) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            drawOutlinedString(g2, t, x, y, Color.BLACK, Color.WHITE, 2);
            g2.dispose();
        }
    }

    /** Custome class for screen creation */
    private static class HomePanel extends JPanel {
        private static final double BASE_W = 1920.0;
        private static final double BASE_H = 1080.0;

        HomePanel() {
            setBackground(new Color(235, 235, 235));
        }

        @Override
        public void doLayout() {
            super.doLayout();

            Component[] comps = getComponents();
            if (comps.length >= 8) {
                JLabel welcomeLabel = (JLabel) comps[0];
                JButton startGameButton = (JButton) comps[1];
                JButton tutorialButton = (JButton) comps[2];
                JButton playerInfoButton = (JButton) comps[3];
                JButton highScoresButton = (JButton) comps[4];
                JButton parentTeacherButton = (JButton) comps[5];
                JButton switchUserButton = (JButton) comps[6];
                JButton exitButton = (JButton) comps[7];

                int w = getWidth();
                int h = getHeight();

                double scale = Math.min(w / BASE_W, h / BASE_H);

                HomeMenuLayout geo = computeHomeMenuLayout(w, h);

                welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, Math.max(20, (int) (28 * scale))));

                int btnPx = Math.max(18, Math.min(38, (int) (geo.buttonH * 0.46)));
                Font buttonFont = new Font("SansSerif", Font.BOLD, btnPx);
                startGameButton.setFont(buttonFont);
                tutorialButton.setFont(buttonFont);
                highScoresButton.setFont(buttonFont);
                playerInfoButton.setFont(buttonFont);
                parentTeacherButton.setFont(buttonFont);
                switchUserButton.setFont(buttonFont);
                exitButton.setFont(buttonFont);

                welcomeLabel.setBounds(0, geo.welcomeLabelY, w, geo.welcomeLabelH);
                startGameButton.setBounds(geo.centerX, geo.startY, geo.buttonW, geo.buttonH);
                tutorialButton.setBounds(geo.centerX, geo.startY + (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
                playerInfoButton.setBounds(geo.centerX, geo.startY + 2 * (geo.buttonH + geo.gap), geo.buttonW,
                        geo.buttonH);
                highScoresButton.setBounds(geo.centerX, geo.startY + 3 * (geo.buttonH + geo.gap), geo.buttonW,
                        geo.buttonH);
                parentTeacherButton.setBounds(geo.centerX, geo.startY + 4 * (geo.buttonH + geo.gap), geo.buttonW,
                        geo.buttonH);
                switchUserButton.setBounds(geo.centerX, geo.startY + 5 * (geo.buttonH + geo.gap), geo.buttonW,
                        geo.buttonH);
                exitButton.setBounds(geo.centerX, geo.startY + 6 * (geo.buttonH + geo.gap), geo.buttonW, geo.buttonH);
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

            g2.setFont(new Font("SansSerif", Font.PLAIN, (int) (85 * Math.min(sx, sy))));
            String title = "DEATH BY SPELL CHECK";
            FontMetrics fmTitle = g2.getFontMetrics();
            int titleX = (w - fmTitle.stringWidth(title)) / 2;
            int titleY = (int) (230 * sy);
            drawOutlinedString(g2, title, titleX, titleY, Color.BLACK, Color.WHITE, 2);

            g2.dispose();
        }

        /** draws lines in custom graphic */
        private static void fillPath(Graphics2D g2, double sx, double sy, int... pts) {
            GeneralPath path = new GeneralPath();
            path.moveTo(pts[0] * sx, pts[1] * sy);
            for (int i = 2; i < pts.length; i += 2) {
                path.lineTo(pts[i] * sx, pts[i + 1] * sy);
            }
            path.closePath();
            g2.fill(path);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Home home = new Home("TestUser", false);
            home.setVisible(true);
        });
    }
}