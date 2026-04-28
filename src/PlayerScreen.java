import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Displays statistical and historical records for players
 * 
 * @author Linsen Liu
 */

public class PlayerScreen extends JPanel {
    private String currentUsername;
    private List<SessionRecord> allSessions;

    private final JFrame frame;

    private JButton backButton;
    private JLabel titleLabel;

    private JLabel totalGamesLabel;
    private JLabel highScoreLabel;
    private JLabel averageWpmLabel;
    private JLabel averageAccuracyLabel;

    private JTable sessionTable;
    private DefaultTableModel tableModel;
    private JScrollPane sessionScrollPane;

    private int hoveredRow = -1;
    private int hoveredColumn = -1;

    /** 
     * PlayerScreen Constructor
     * 
     * @param frame A {@link javax.swing.JFrame} to contain this panel
     */
    public PlayerScreen(JFrame frame) {
        this.frame = frame;
        this.allSessions = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(new Color(230, 230, 230));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initializeComponents();
        buildLayout();
        registerEvents();
    }

    /** 
     * Updates the screen with specified the player data
     * 
     * @param userName A String with the player's username
     */
    public void setData(String userName, List<SessionRecord> sessions) {
        this.currentUsername = userName;
        this.allSessions = sessions != null ? sessions : new ArrayList<>();
        refreshScreen();
    }

    /** Initializes all Java Swing components */
    private void initializeComponents() {
        backButton = createStyledButton("BACK TO MENU", 18, new Dimension(220, 55));

        titleLabel = new JLabel("PLAYER SCREEN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 42));
        titleLabel.setForeground(Color.BLACK);

        totalGamesLabel = createStatLabel("* Total Games: 0");
        highScoreLabel = createStatLabel("* High Score: 0");
        averageWpmLabel = createStatLabel("* Average WPM: 0.00");
        averageAccuracyLabel = createStatLabel("* Average Accuracy: 0.00%");

        tableModel = new DefaultTableModel(
                new Object[]{"DATE", "DIFFICULTY", "SCORE", "ACCURACY", "WPM"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sessionTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (c instanceof JComponent comp) {
                    comp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                }

                if (isRowSelected(row)) {
                    c.setBackground(new Color(210, 210, 210));
                    c.setForeground(Color.BLACK);
                    c.setFont(new Font("SansSerif", Font.BOLD, 15));
                } else if (row == hoveredRow) {
                    if (column == hoveredColumn) {
                        c.setBackground(new Color(185, 185, 185));
                        c.setForeground(Color.BLACK);
                        c.setFont(new Font("SansSerif", Font.BOLD, 16));
                        if (c instanceof JComponent comp) {
                            comp.setBorder(new LineBorder(Color.BLACK, 2));
                        }
                    } else {
                        c.setBackground(new Color(225, 225, 225));
                        c.setForeground(Color.BLACK);
                        c.setFont(new Font("SansSerif", Font.BOLD, 15));
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    c.setFont(new Font("SansSerif", Font.BOLD, 15));
                }

                return c;
            }
        };

        sessionTable.setRowHeight(34);
        sessionTable.setPreferredScrollableViewportSize(new Dimension(800, sessionTable.getRowHeight() * 10));
        sessionTable.setGridColor(Color.BLACK);
        sessionTable.setShowGrid(true);
        sessionTable.setSelectionBackground(new Color(210, 210, 210));
        sessionTable.setSelectionForeground(Color.BLACK);
        sessionTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        sessionTable.getTableHeader().setBackground(Color.WHITE);
        sessionTable.getTableHeader().setForeground(Color.BLACK);
        sessionTable.getTableHeader().setBorder(new LineBorder(Color.BLACK, 2));
        sessionTable.getTableHeader().setReorderingAllowed(false);
        sessionTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sessionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        sessionTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        sessionTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        sessionTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        sessionTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        sessionTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        ((javax.swing.table.DefaultTableCellRenderer) sessionTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < sessionTable.getColumnCount(); i++) {
            sessionTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        sessionScrollPane = new JScrollPane(sessionTable);
        sessionScrollPane.setBorder(new LineBorder(Color.BLACK, 3));
        sessionScrollPane.getViewport().setBackground(Color.WHITE);
        sessionScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        sessionScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    }

    /** Creates the final layout and assembles the components */
    private void buildLayout() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(new LogoPanel());
        topBar.add(rightPanel, BorderLayout.EAST);

        JPanel leftDummy = new JPanel();
        leftDummy.setPreferredSize(new Dimension(170, 120));
        leftDummy.setOpaque(false);
        topBar.add(leftDummy, BorderLayout.WEST);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 0, 18, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        topBar.add(headerPanel, BorderLayout.CENTER);

        JPanel leftStatsPanel = createStatsPanel();
        JPanel rightSessionsPanel = createSessionsPanel();

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.0;

        gbc.gridx = 0;
        gbc.weightx = 0.33;
        gbc.insets = new Insets(0, 0, 0, 12);
        contentPanel.add(leftStatsPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.67;
        gbc.insets = new Insets(0, 12, 0, 0);
        contentPanel.add(rightSessionsPanel, gbc);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerWrapper.add(contentPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);

        add(topBar, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** Registers button events */
    private void registerEvents() {
        backButton.addActionListener(e -> {
            if (frame != null) {
                frame.dispose();
            }
        });

        sessionTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredRow = sessionTable.rowAtPoint(e.getPoint());
                hoveredColumn = sessionTable.columnAtPoint(e.getPoint());
                sessionTable.repaint();
            }
        });

        sessionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                hoveredColumn = -1;
                sessionTable.repaint();
            }
        });
    }

    /** Updates all labels and the table data */
    private void refreshScreen() {
        List<SessionRecord> mySessions = getCurrentUserSessions();

        if (currentUsername == null || currentUsername.isBlank()) {
            titleLabel.setText("PLAYER SCREEN");
        } else {
            titleLabel.setText("PLAYER SCREEN FOR " + currentUsername.toUpperCase());
        }

        if (mySessions.isEmpty()) {
            totalGamesLabel.setText("* Total Games: N/A");
            highScoreLabel.setText("* High Score: N/A");
            averageWpmLabel.setText("* Average WPM: N/A");
            averageAccuracyLabel.setText("* Average Accuracy: N/A");
        } else {
            totalGamesLabel.setText("* Total Games: " + mySessions.size());
            highScoreLabel.setText("* High Score: " + calculateHighScore(mySessions));
            averageWpmLabel.setText(String.format("* Average WPM: %.2f", calculateAverageWpm(mySessions)));
            averageAccuracyLabel.setText(String.format("* Average Accuracy: %.2f%%", calculateAverageAccuracy(mySessions)));
        }

        refreshTable(mySessions);
    }

    /**
     * Filters the list for records matching the current user
     * 
     * @return A list of session belonging to the current player
     */
    private List<SessionRecord> getCurrentUserSessions() {
        List<SessionRecord> result = new ArrayList<>();

        if (currentUsername == null || currentUsername.isBlank()) {
            return result;
        }

        for (SessionRecord session : allSessions) {
            if (session.getUsername() != null
                    && session.getUsername().equalsIgnoreCase(currentUsername)) {
                result.add(session);
            }
        }

        result.sort(Comparator.comparing(SessionRecord::getDate).reversed());
        return result;
    }


    /** 
     * Finds the highest score among the provided sessions
     * 
     * @param sessions The list of all sessions
     * @return The highest score
     */
    private int calculateHighScore(List<SessionRecord> sessions) {
        int max = 0;
        for (SessionRecord session : sessions) {
            if (session.getScore() > max) {
                max = session.getScore();
            }
        }
        return max;
    }

    /** 
     * Calculates the mean Words Per Minute across sessions
     * 
     * @param sessions The list of all sessions
     * @return A double as the average WPM
     */
    private double calculateAverageWpm(List<SessionRecord> sessions) {
        if (sessions.isEmpty()) {
            return 0.0;
        }

        int sum = 0;
        for (SessionRecord session : sessions) {
            sum += session.getWpm();
        }
        return (double) sum / sessions.size();
    }

    /**
     * Finds the average accuracy of the play over their sessions
     * 
     * @param sessions The list of all sessions
     * @return A double as the average accuracy
     */
    private double calculateAverageAccuracy(List<SessionRecord> sessions) {
        if (sessions.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (SessionRecord session : sessions) {
            sum += session.getAccuracy();
        }
        return sum / sessions.size();
    }

    /**
     * Rebuilds the table with the player's sessions
     * 
     * @param sessions The list of all sessions
     */
    private void refreshTable(List<SessionRecord> sessions) {
        tableModel.setRowCount(0);

        if (sessions.isEmpty()) {
            JLabel emptyLabel = new JLabel("There is no past game", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
            emptyLabel.setForeground(Color.BLACK);
            emptyLabel.setOpaque(true);
            emptyLabel.setBackground(Color.WHITE);
            sessionScrollPane.setViewportView(emptyLabel);
            return;
        }

        sessionScrollPane.setViewportView(sessionTable);

        for (SessionRecord session : sessions) {
            tableModel.addRow(new Object[]{
                    session.getFormattedDate(),
                    session.getDifficulty(),
                    session.getScore(),
                    String.format("%.2f%%", session.getAccuracy()),
                    session.getWpm()
            });
        }
    }

    /**
     * Creates and formats the panel containing the statistics
     * 
     * @return A {@link javax.swing.JPanel} formatted
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                new EmptyBorder(25, 25, 25, 25)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel sectionTitle = new JLabel("PLAYER DATA");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        sectionTitle.setForeground(Color.BLACK);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(25));
        panel.add(totalGamesLabel);
        panel.add(Box.createVerticalStrut(18));
        panel.add(highScoreLabel);
        panel.add(Box.createVerticalStrut(18));
        panel.add(averageWpmLabel);
        panel.add(Box.createVerticalStrut(18));
        panel.add(averageAccuracyLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Creates and formats the panel containing the session history
     * 
     * @return A {@link javax.swing.JPanel} formatted
     */
    private JPanel createSessionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel sectionTitle = new JLabel("RECENT SESSIONS HISTORY");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        sectionTitle.setForeground(Color.BLACK);

        panel.add(sectionTitle, BorderLayout.NORTH);
        panel.add(sessionScrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Formats panel text
     * 
     * @param text The text to be formatted
     * @return A {@link javax.swing.JPanel} formatted
     */
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Creates the buttons
     * 
     * @return {@link javax.swing.JButton} formatted
     */
    private JButton createStyledButton(String text, int fontSize, Dimension size) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                new EmptyBorder(8, 12, 8, 12)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        return button;
    }

    /** Custom class for the logo */
    private static class LogoPanel extends JPanel {
        private static final double ORIGINAL_W = 1920.0;
        private static final double ORIGINAL_H = 1080.0;

        LogoPanel() {
            setPreferredSize(new Dimension(170, 120));
            setMinimumSize(new Dimension(170, 120));
            setMaximumSize(new Dimension(170, 120));
            setOpaque(false);
        }

        /** Draws the logo */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2.setColor(Color.BLACK);

            double sx = getWidth() / ORIGINAL_W;
            double sy = getHeight() / ORIGINAL_H;

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

        /** fills in between the points in the logo 
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
    }
}
