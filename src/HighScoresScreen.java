import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Displays and manages player rankings and high scores
 * 
 * @author Boyu Yang
 */

public class HighScoresScreen extends JPanel {

    private static final int MAX_ROWS = 10;

    private List<SessionRecord> allSessions = new ArrayList<>();
    private JPanel scoresPanel;

    private final Map<String, JButton> difficultyButtons = new LinkedHashMap<>();

    /** 
     * HishScoresScreen Constructor
     * 
     * @param onBack Callback to return to the main menu
     */
    public HighScoresScreen(Runnable onBack) {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(235, 235, 235));
 
        add(build_TitleLabel(), BorderLayout.NORTH);
        add(build_DifficultyTabs(), BorderLayout.CENTER);
 
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setOpaque(false);
        backPanel.add(build_BackButton(onBack));
        add(backPanel, BorderLayout.SOUTH);

        loadScores();
        showScoresFor("EASY");
    }

    /** Creates and formats the main title for the screen 
     * 
     * @return title A {@link javax.swing.JLabel} containg the title
    */
    private JLabel build_TitleLabel() {
        JLabel title = new JLabel("HIGH SCORES", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.PLAIN, 42));
        title.setForeground(Color.BLACK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return title;
    }

    /**
     * Builds the interface used to switch between difficulty levels
     * 
     * @return A {@link javax.swing.JLabel} with the diffculty button and scores displayed
     */
    private JPanel build_DifficultyTabs() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JPanel tabs = new JPanel(new GridLayout(1, 3, 5, 0));
        tabs.setOpaque(false);

        String[] difficulties = {"EASY", "MEDIUM", "HARD"};
        for (String diff : difficulties) {
            JButton btn = createMenuButton(diff);
            btn.addActionListener(e -> showScoresFor(diff));
            difficultyButtons.put(diff, btn);
            tabs.add(btn);
        }

        scoresPanel = new JPanel(new BorderLayout());
        scoresPanel.setOpaque(false);

        wrapper.add(tabs, BorderLayout.NORTH);
        wrapper.add(scoresPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton build_BackButton(Runnable onBack) {
        JButton back = createMenuButton("BACK TO MENU");
        back.addActionListener(e -> onBack.run());
        return back;
    }

    /** 
     * Creates the menu buttons
     * 
     * @param text The text on the button
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
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                new javax.swing.border.EmptyBorder(8, 12, 8, 12)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        return button;
    }

    /**
     * Updates difficulty tab buttons
     * 
     * @param activeDifficulty A String containing the current difficulty level
    */
    private void updateTabStyles(String activeDifficulty) {
        for (Map.Entry<String, JButton> entry : difficultyButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(activeDifficulty)) {
                btn.setBackground(Color.BLACK);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
            }
        }
    }

    /** 
     * Filters and displays the top scores
     * 
     * @param difficulty A String containing the difficulty to sort by
     */
    private void showScoresFor(String difficulty) {
        updateTabStyles(difficulty);

        scoresPanel.removeAll();

        Comparator<SessionRecord> order = Comparator
                .comparingInt(SessionRecord::getScore).reversed()
                .thenComparing(SessionRecord::getDate, Comparator.reverseOrder());

        List<SessionRecord> filtered = allSessions.stream()
                .filter(s -> s.getDifficulty() != null
                        && s.getDifficulty().equalsIgnoreCase(difficulty))
                .sorted(order)
                .limit(MAX_ROWS)
                .collect(Collectors.toList());

        if (allSessions.isEmpty() || filtered.isEmpty()) {
            String msg = allSessions.isEmpty()
                    ? "No score history yet"
                    : "No scores for this difficulty yet";

            JLabel emptyLabel = new JLabel(msg, SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setOpaque(true);
            emptyLabel.setBackground(Color.WHITE);

            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Color.BLACK, 4),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)));
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            scoresPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            scoresPanel.add(build_ScoresTable(filtered, difficulty), BorderLayout.CENTER);
        }

        scoresPanel.revalidate();
        scoresPanel.repaint();
    }

    /** 
     * Constructs the score table
     * 
     * 
     * @param records A list of the session records to populate the table
     * @param difficulty The difficulty level for the table header
     * @return A {@link javax.swing.JPanel} containing the formatted scores table
     */
    private JPanel build_ScoresTable(List<SessionRecord> records, String difficulty) {
        final int[] hoveredRow = {-1};
        final int[] hoveredColumn = {-1};

        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
                new Object[]{"Rank", "User", "Score", "Acc %", "WPM", "Date", "Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel) {
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
                } else if (row == hoveredRow[0]) {
                    if (column == hoveredColumn[0]) {
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

        table.setRowHeight(34);
        table.setGridColor(Color.BLACK);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(210, 210, 210));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setBorder(new LineBorder(Color.BLACK, 2));
        table.getTableHeader().setReorderingAllowed(false);
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        ((javax.swing.table.DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                hoveredRow[0] = table.rowAtPoint(e.getPoint());
                hoveredColumn[0] = table.columnAtPoint(e.getPoint());
                table.repaint();
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredRow[0] = -1;
                hoveredColumn[0] = -1;
                table.repaint();
            }
        });

        for (int i = 0; i < records.size(); i++) {
            SessionRecord s = records.get(i);
            String acc = String.format(Locale.US, "%.1f%%", s.getAccuracy());
            String datePart = s.getDate() != null && s.getDate().length() >= 10
                    ? s.getDate().substring(0, 10)
                    : (s.getDate() != null ? s.getDate() : "");
            String timePart = "";
            if (s.getDate() != null && s.getDate().length() > 10) {
                timePart = s.getDate().substring(10).trim();
                if (timePart.startsWith("T")) {
                    timePart = timePart.substring(1);
                }
            }
            tableModel.addRow(new Object[]{
                    i + 1,
                    truncate(s.getUsername(), 14),
                    s.getScore(),
                    acc,
                    s.getWpm(),
                    datePart,
                    truncate(timePart, 12)
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(Color.BLACK, 3));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                new javax.swing.border.EmptyBorder(15, 15, 15, 15)));

        JLabel sectionTitle = new JLabel("TOP " + MAX_ROWS + " " + difficulty + " HIGH SCORES");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        sectionTitle.setForeground(Color.BLACK);

        panel.add(sectionTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /** 
     * Truncates a string to a specified length
     * 
     * @param t The String to truncate
     * @param max The maximum allowed length
     * @return A String formatted to the correct length
     */
    private static String truncate(String t, int max) {
        if (t == null) {
            return "";
        }
        String s = t.trim();
        return s.length() <= max ? s : s.substring(0, max - 1) + "\u2026";
    }

    /** Reloads all session records */
    private void loadScores() {
        allSessions.clear();
        try {
            allSessions = SessionRecord.loadAllSessions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
