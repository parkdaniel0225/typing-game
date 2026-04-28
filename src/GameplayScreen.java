import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Creates the main gameplay screen for the program, allows the user to play the game
 * 
 * @author Suvashish Saha
 */

public class GameplayScreen extends JPanel {
    private static final Color BG = new Color(235, 235, 235);
    private static final Color TEXT = new Color(35, 35, 35);
    private static final Color ACCENT = new Color(35, 60, 115);
    private static final Color SUCCESS = new Color(25, 130, 55);
    private static final Color ERROR = new Color(180, 40, 40);
    private static final Color WARNING = new Color(210, 110, 0);
    private static final Color POWER_COLOR = new Color(125, 70, 20);

    private static final int DEFINITION_READ_DELAY_MS = 2500;

    private static final int MAX_DEFINITION_DISPLAY_CHARS = 160;

    private static final int FEEDBACK_SCROLL_MAX_HEIGHT = 96;

    private static final int DEFINITION_SCROLL_MAX_HEIGHT = 280;

    private static final String CARD_PRESTART = "PRESTART";
    private static final String CARD_GAME = "GAME";
    private static final String CARD_GAMEOVER = "GAMEOVER";

    private final Runnable onBack;
    private final Random random = new Random();

    private String currentUsername = "";
    private String difficulty = "MEDIUM";
    private String selectedDifficulty = "MEDIUM";
    private List<SessionRecord> sessions = new ArrayList<SessionRecord>();

    private final CardLayout screenLayout = new CardLayout();
    private JPanel screenPanel;

    private JPanel preStartPanel;
    private JLabel preStartTitleLabel;
    private JLabel preStartUserLabel;
    private JLabel selectedDifficultyLabel;
    private JButton easyButton;
    private JButton mediumButton;
    private JButton hardButton;
    private JButton startGameButton;
    private JButton preStartBackButton;

    private JPanel gamePanel;
    private JLabel titleLabel;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel difficultyLabel;

    private JLabel timerTitleLabel;
    private JLabel timerValueLabel;
    private JLabel powerUpTitleLabel;
    private JLabel powerUpValueLabel;

    private JLabel feedbackLabel;
    private JScrollPane feedbackScrollPane;
    private JPanel definitionSectionPanel;
    private JScrollPane definitionScrollPane;
    private JLabel definitionLabel;
    private JLabel countdownLabel;
    private JLabel hintLabel;
    private JPanel slotsPanel;
    private JPanel lettersPanel;

    private JButton clearButton;
    private JButton removeButton;
    private JButton pauseButton;

    private JPanel pauseOverlayPanel;
    private JButton pauseResumeButton;
    private JButton pauseChangeDifficultyButton;
    private JButton pauseRestartButton;
    private JButton pauseBackToMenuButton;

    private JPanel gameOverPanel;
    private JLabel gameOverTitleLabel;
    private JLabel gameOverStatsLabel;
    private JEditorPane gameOverWordsPane;
    private JButton playAgainButton;
    private JButton changeDifficultyButton;
    private JButton backToMenuButton;

    private List<String> easyWords = new ArrayList<String>();
    private List<String> mediumWords = new ArrayList<String>();
    private List<String> hardWords = new ArrayList<String>();
    private List<String> remainingWords = new ArrayList<String>();
    private String lastServedWord = null;

    private Set<String> validationWords = new HashSet<String>();

    private String currentWord = "";
    private StringBuilder currentAttempt = new StringBuilder();
    private final List<JLabel> slotLabels = new ArrayList<JLabel>();
    private final List<LetterCircleButton> letterButtons = new ArrayList<LetterCircleButton>();
    private final List<LetterCircleButton> pickSequence = new ArrayList<LetterCircleButton>();

    private int score = 0;
    private int lives = 3;
    private int totalAttempts = 0;
    private int correctAttempts = 0;
    private long gameStartTimeMillis;

    private final LinkedHashSet<String> unsolvedWords = new LinkedHashSet<String>();

    private double timeLimit = 9.0;
    private double timeRemaining = 9.0;

    private boolean gameOver = false;
    private boolean acceptingInput = false;
    private boolean countdownRunning = false;
    private boolean betweenWords = false;

    private final List<PowerUp> activePowerUps = new ArrayList<PowerUp>();
    private int wordsUntilNextPowerUp = 1 + random.nextInt(3);
    private boolean powerUpChanceThisWord = false;
    private boolean pendingBonusIsSlowTimer = true;

    private String lastPowerUpHtmlCache = null;

    private Timer gameTimer;
    private Timer countdownTimer;
    private Timer wordTransitionTimer;

    private boolean paused;
    private boolean gameTimerWasRunning;

    /**
     * GameplayScreen Constructor
     *
     * @param onBack Callback to return to the main menu.
     */
    public GameplayScreen(Runnable onBack) {
        this.onBack = onBack;

        loadAllWordFiles();

        setLayout(new BorderLayout());
        setBackground(BG);
        setFocusable(true);

        initializeComponents();
        buildLayout();
        registerEvents();
        setupKeyBindings();
        createTimers();
    }

    /**
    * Updates the player data and prepares the screen
    * 
    * @param username The usernam of the current player
    * @param sessions The list of past sessions
    */
    public void setData(String username, List<SessionRecord> sessions) {
        setData(username, sessions, "MEDIUM");
    }

    /**
    * Updates the player data and prepares the screen
    * 
    * @param username The usernam of the current player
    * @param sessions The list of past sessions
    * @param difficulty The difficulty selected
    */
    public void setData(String username, List<SessionRecord> sessions, String difficulty) {
        this.currentUsername = username != null ? username : "";
        this.sessions = sessions != null ? sessions : new ArrayList<SessionRecord>();
        this.selectedDifficulty = normalizeDifficulty(difficulty);
        this.difficulty = this.selectedDifficulty;
        showPreStartScreen();
    }

    /** Initializes screen componets */
    private void initializeComponents() {
        preStartPanel = new TemplatePanel();
        preStartPanel.setLayout(new BoxLayout(preStartPanel, BoxLayout.Y_AXIS));
        preStartPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        preStartTitleLabel = new JLabel("DEATH BY SPELL CHECK", SwingConstants.CENTER);
        preStartTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        preStartTitleLabel.setForeground(TEXT);
        preStartTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        preStartUserLabel = new JLabel("Player: ", SwingConstants.CENTER);
        preStartUserLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        preStartUserLabel.setForeground(TEXT);
        preStartUserLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectedDifficultyLabel = new JLabel("Selected Difficulty: MEDIUM", SwingConstants.CENTER);
        selectedDifficultyLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        selectedDifficultyLabel.setForeground(ACCENT);
        selectedDifficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        easyButton = createMenuButton("EASY");
        mediumButton = createMenuButton("MEDIUM");
        hardButton = createMenuButton("HARD");
        startGameButton = createMenuButton("START GAME");
        preStartBackButton = createMenuButton("BACK TO MENU");

        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(BG);
        gamePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        titleLabel = new JLabel("DEATH BY SPELL CHECK", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(TEXT);

        scoreLabel = createTopLabel("Score: 0");
        livesLabel = createTopLabel("Lives: 3");
        difficultyLabel = createTopLabel("Difficulty: MEDIUM");
        livesLabel.setText(formatLivesHtml(3));

        timerTitleLabel = new JLabel("TIMER", SwingConstants.CENTER);
        timerTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        timerTitleLabel.setForeground(TEXT);

        timerValueLabel = new JLabel("9.0", SwingConstants.CENTER);
        timerValueLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 56));
        timerValueLabel.setForeground(TEXT);

        powerUpTitleLabel = new JLabel("POWER-UPS", SwingConstants.CENTER);
        powerUpTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        powerUpTitleLabel.setForeground(TEXT);

        powerUpValueLabel = new JLabel("<html><div style='text-align:center;font-size:18pt;'>Loading…</div></html>", SwingConstants.CENTER);
        powerUpValueLabel.setForeground(POWER_COLOR);

        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        feedbackLabel.setForeground(ACCENT);
        feedbackLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
        feedbackLabel.setVerticalAlignment(SwingConstants.TOP);

        definitionLabel = new JLabel(" ", SwingConstants.CENTER);
        definitionLabel.setFont(new Font("SansSerif", Font.PLAIN, 17));
        definitionLabel.setForeground(new Color(55, 55, 55));
        definitionLabel.setVerticalAlignment(SwingConstants.TOP);

        countdownLabel = new JLabel(" ", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("SansSerif", Font.BOLD, 64));
        countdownLabel.setForeground(ACCENT);
        countdownLabel.setVisible(false);

        hintLabel = new JLabel(" ", SwingConstants.CENTER);
        hintLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        hintLabel.setForeground(new Color(25, 55, 130));

        slotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        slotsPanel.setOpaque(false);

        lettersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        lettersPanel.setOpaque(false);

        clearButton = createMenuButton("CLEAR");
        removeButton = createMenuButton("REMOVE");
        pauseButton = createMenuButton("PAUSE");

        pauseResumeButton = createMenuButton("RESUME");
        pauseChangeDifficultyButton = createMenuButton("CHANGE DIFFICULTY");
        pauseRestartButton = createMenuButton("RESTART");
        pauseBackToMenuButton = createMenuButton("BACK TO MENU");

        gameOverPanel = new TemplatePanel();
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
        gameOverPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        gameOverTitleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        gameOverTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        gameOverTitleLabel.setForeground(TEXT);
        gameOverTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gameOverStatsLabel = new JLabel(" ", SwingConstants.CENTER);
        gameOverStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        gameOverStatsLabel.setForeground(TEXT);
        gameOverStatsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gameOverWordsPane = new JEditorPane();
        gameOverWordsPane.setContentType("text/html");
        gameOverWordsPane.setEditable(false);
        gameOverWordsPane.setOpaque(false);
        gameOverWordsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        gameOverWordsPane.setFont(new Font("SansSerif", Font.PLAIN, 15));

        playAgainButton = createMenuButton("PLAY AGAIN");
        changeDifficultyButton = createMenuButton("CHANGE DIFFICULTY");
        backToMenuButton = createMenuButton("BACK TO MENU");
    }

    /** Creates screen layout */
    private void buildLayout() {
        screenPanel = new JPanel(screenLayout);
        screenPanel.setOpaque(false);

        buildPreStartPanel();
        buildGamePanel();
        buildGameOverPanel();

        screenPanel.add(preStartPanel, CARD_PRESTART);
        screenPanel.add(gamePanel, CARD_GAME);
        screenPanel.add(gameOverPanel, CARD_GAMEOVER);

        add(screenPanel, BorderLayout.CENTER);
    }

    /**
     * Creates the fromated panel
     * 
     * @return A {@link javax.swing.JPanel} with formatting
     */
    private JPanel createOverlayPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(new Color(50, 50, 50, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(35, 45, 35, 45));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(760, 520));
        return panel;
    }

    /** Configures pre-gameplay panel layout */
    private void buildPreStartPanel() {
        preStartPanel.removeAll();
        preStartPanel.setLayout(new BoxLayout(preStartPanel, BoxLayout.Y_AXIS));

        JPanel difficultyButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        difficultyButtonsPanel.setOpaque(false);
        difficultyButtonsPanel.add(easyButton);
        difficultyButtonsPanel.add(mediumButton);
        difficultyButtonsPanel.add(hardButton);

        JPanel startButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        startButtonsPanel.setOpaque(false);
        startButtonsPanel.add(startGameButton);
        startButtonsPanel.add(preStartBackButton);

        JLabel instruction = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Choose your difficulty before the game starts"
                        + "</div></html>",
                SwingConstants.CENTER
        );
        instruction.setFont(new Font("SansSerif", Font.BOLD, 24));
        instruction.setForeground(TEXT);
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel overlay = createOverlayPanel();

        overlay.add(preStartTitleLabel);
        overlay.add(Box.createVerticalStrut(18));
        overlay.add(preStartUserLabel);
        overlay.add(Box.createVerticalStrut(24));
        overlay.add(instruction);
        overlay.add(Box.createVerticalStrut(24));
        overlay.add(selectedDifficultyLabel);
        overlay.add(Box.createVerticalStrut(20));
        overlay.add(difficultyButtonsPanel);
        overlay.add(Box.createVerticalStrut(26));
        overlay.add(startButtonsPanel);

        preStartPanel.add(Box.createVerticalGlue());
        preStartPanel.add(overlay);
        preStartPanel.add(Box.createVerticalGlue());
    }

    /** Configures gameplay panel layout */
    private void buildGamePanel() {
        gamePanel.removeAll();
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(BG);
        gamePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titleWrapper.setOpaque(false);
        titleWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleWrapper.add(titleLabel);

        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 36, 8));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsRow.add(difficultyLabel);
        statsRow.add(scoreLabel);
        statsRow.add(livesLabel);

        JPanel timerPowerPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        timerPowerPanel.setOpaque(false);
        timerPowerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerPowerPanel.setBorder(new EmptyBorder(2, 8, 0, 8));

        JPanel timerPanel = new JPanel();
        timerPanel.setOpaque(true);
        timerPanel.setBackground(BG);
        timerPanel.setBorder(new LineBorder(new Color(25, 25, 25), 1));
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerPanel.add(Box.createVerticalStrut(1));
        timerPanel.add(timerTitleLabel);
        timerPanel.add(Box.createVerticalStrut(1));
        timerPanel.add(timerValueLabel);
        timerPanel.add(Box.createVerticalStrut(1));

        JPanel powerPanel = new JPanel();
        powerPanel.setOpaque(true);
        powerPanel.setBackground(BG);
        powerPanel.setBorder(new LineBorder(new Color(25, 25, 25), 1));
        powerPanel.setLayout(new BoxLayout(powerPanel, BoxLayout.Y_AXIS));
        powerUpTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        powerUpValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        powerPanel.add(Box.createVerticalStrut(1));
        powerPanel.add(powerUpTitleLabel);
        powerPanel.add(Box.createVerticalStrut(2));
        powerPanel.add(powerUpValueLabel);
        powerPanel.add(Box.createVerticalStrut(2));

        timerPowerPanel.add(timerPanel);
        timerPowerPanel.add(powerPanel);

        topPanel.add(titleWrapper);
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(statsRow);
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(timerPowerPanel);

        feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        countdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        feedbackScrollPane = new JScrollPane(feedbackLabel) {
            @Override
            public Dimension getPreferredSize() {
                Dimension lp = feedbackLabel.getPreferredSize();
                if (lp.width <= 0 || lp.height <= 0) {
                    lp = new Dimension(520, 40);
                }
                int h = Math.min(lp.height + 6, FEEDBACK_SCROLL_MAX_HEIGHT);
                int w = Math.max(lp.width, 400);
                return new Dimension(w, h);
            }
        };
        feedbackScrollPane.setBorder(BorderFactory.createEmptyBorder());
        feedbackScrollPane.setOpaque(false);
        feedbackScrollPane.getViewport().setOpaque(false);
        feedbackScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        feedbackScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedbackScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        definitionScrollPane = new JScrollPane(definitionLabel) {
            @Override
            public Dimension getPreferredSize() {
                Dimension lp = definitionLabel.getPreferredSize();
                if (lp.width <= 0 || lp.height <= 0) {
                    lp = new Dimension(520, 36);
                }
                int h = Math.min(lp.height + 8, DEFINITION_SCROLL_MAX_HEIGHT);
                int w = Math.max(lp.width, 400);
                return new Dimension(w, h);
            }
        };
        definitionScrollPane.setBorder(BorderFactory.createEmptyBorder());
        definitionScrollPane.setOpaque(false);
        definitionScrollPane.getViewport().setOpaque(false);
        definitionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        definitionScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        definitionScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        definitionSectionPanel = new JPanel(new BorderLayout());
        definitionSectionPanel.setOpaque(false);
        TitledBorder defTitle = BorderFactory.createTitledBorder(
                new LineBorder(new Color(25, 25, 25), 2),
                "Definition",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 17),
                TEXT);
        definitionSectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 2, 0),
                defTitle));
        definitionSectionPanel.add(definitionScrollPane, BorderLayout.CENTER);
        definitionSectionPanel.setVisible(false);

        JPanel northStack = new JPanel();
        northStack.setOpaque(false);
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        feedbackScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        definitionSectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        northStack.add(feedbackScrollPane);
        northStack.add(Box.createVerticalStrut(6));
        northStack.add(definitionSectionPanel);

        JPanel gameStack = new JPanel();
        gameStack.setOpaque(false);
        gameStack.setLayout(new BoxLayout(gameStack, BoxLayout.Y_AXIS));

        slotsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lettersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        actionPanel.setOpaque(false);
        actionPanel.add(clearButton);
        actionPanel.add(removeButton);
        actionPanel.add(pauseButton);
        actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gameStack.add(Box.createVerticalStrut(4));
        gameStack.add(countdownLabel);
        gameStack.add(Box.createVerticalStrut(2));
        gameStack.add(hintLabel);
        gameStack.add(Box.createVerticalStrut(6));
        gameStack.add(slotsPanel);
        gameStack.add(Box.createVerticalStrut(8));
        gameStack.add(lettersPanel);

        JPanel gameStackBottom = new JPanel(new BorderLayout());
        gameStackBottom.setOpaque(false);
        gameStackBottom.add(gameStack, BorderLayout.SOUTH);

        JPanel centerColumn = new JPanel(new BorderLayout());
        centerColumn.setOpaque(false);
        centerColumn.add(northStack, BorderLayout.NORTH);
        centerColumn.add(gameStackBottom, BorderLayout.CENTER);

        JPanel bottomControls = new JPanel();
        bottomControls.setOpaque(false);
        bottomControls.setLayout(new BoxLayout(bottomControls, BoxLayout.Y_AXIS));
        bottomControls.add(actionPanel);

        JPanel gameContent = new JPanel(new BorderLayout());
        gameContent.setOpaque(false);
        gameContent.add(topPanel, BorderLayout.NORTH);
        gameContent.add(centerColumn, BorderLayout.CENTER);
        gameContent.add(bottomControls, BorderLayout.SOUTH);

        pauseOverlayPanel = new JPanel(new GridBagLayout());
        pauseOverlayPanel.setBackground(Color.BLACK);
        pauseOverlayPanel.setOpaque(true);
        pauseOverlayPanel.setVisible(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.CENTER;

        JPanel pauseCenter = new JPanel();
        pauseCenter.setOpaque(false);
        pauseCenter.setLayout(new BoxLayout(pauseCenter, BoxLayout.Y_AXIS));

        PauseOutlinedTitleLabel pauseTitle = new PauseOutlinedTitleLabel("DEATH BY SPELL CHECK");
        pauseTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseCenter.add(pauseTitle);
        pauseCenter.add(Box.createVerticalStrut(40));

        pauseResumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseChangeDifficultyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseRestartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseBackToMenuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseCenter.add(pauseResumeButton);
        pauseCenter.add(Box.createVerticalStrut(14));
        pauseCenter.add(pauseChangeDifficultyButton);
        pauseCenter.add(Box.createVerticalStrut(14));
        pauseCenter.add(pauseRestartButton);
        pauseCenter.add(Box.createVerticalStrut(14));
        pauseCenter.add(pauseBackToMenuButton);

        pauseOverlayPanel.add(pauseCenter, gc);

        final JLayeredPane layered = new JLayeredPane();
        layered.setOpaque(true);
        layered.setBackground(BG);
        layered.add(gameContent, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
        layered.add(pauseOverlayPanel, Integer.valueOf(JLayeredPane.PALETTE_LAYER));

        layered.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layered.getWidth();
                int h = layered.getHeight();
                if (w <= 0 || h <= 0) {
                    return;
                }
                gameContent.setBounds(0, 0, w, h);
                pauseOverlayPanel.setBounds(0, 0, w, h);
            }
        });

        gamePanel.add(layered, BorderLayout.CENTER);
    }

    /** Configures game over panel layout */
    private void buildGameOverPanel() {
        gameOverPanel.removeAll();
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(playAgainButton);
        buttonRow.add(changeDifficultyButton);
        buttonRow.add(backToMenuButton);

        JPanel overlay = createOverlayPanel();
        overlay.setMaximumSize(new Dimension(780, 640));

        JScrollPane wordsScroll = new JScrollPane(gameOverWordsPane);
        wordsScroll.setPreferredSize(new Dimension(640, 200));
        wordsScroll.setMaximumSize(new Dimension(760, 240));
        wordsScroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsScroll.setBorder(BorderFactory.createEmptyBorder());
        wordsScroll.getViewport().setOpaque(false);
        wordsScroll.setOpaque(false);

        overlay.add(gameOverTitleLabel);
        overlay.add(Box.createVerticalStrut(20));
        overlay.add(gameOverStatsLabel);
        overlay.add(Box.createVerticalStrut(16));
        overlay.add(wordsScroll);
        overlay.add(Box.createVerticalStrut(20));
        overlay.add(buttonRow);

        gameOverPanel.add(Box.createVerticalGlue());
        gameOverPanel.add(overlay);
        gameOverPanel.add(Box.createVerticalGlue());
    }

    /** Button events */
    private void registerEvents() {
        easyButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDifficulty = "EASY";
                updateDifficultySelectionUI();
                requestFocusInWindow();
            }
        });

        mediumButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDifficulty = "MEDIUM";
                updateDifficultySelectionUI();
                requestFocusInWindow();
            }
        });

        hardButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDifficulty = "HARD";
                updateDifficultySelectionUI();
                requestFocusInWindow();
            }
        });

        startGameButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                difficulty = selectedDifficulty;
                resetGame();
                screenLayout.show(screenPanel, CARD_GAME);
                requestFocusInWindow();
            }
        });

        preStartBackButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAllTimers();
                if (onBack != null) {
                    onBack.run();
                }
            }
        });

        clearButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAttempt();
            }
        });

        removeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeLastLetter();
            }
        });

        pauseButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseGame();
            }
        });

        pauseResumeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeGame();
            }
        });

        pauseChangeDifficultyButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paused = false;
                if (pauseOverlayPanel != null) {
                    pauseOverlayPanel.setVisible(false);
                }
                stopAllTimers();
                showPreStartScreen();
            }
        });

        pauseRestartButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paused = false;
                if (pauseOverlayPanel != null) {
                    pauseOverlayPanel.setVisible(false);
                }
                stopAllTimers();
                resetGame();
                screenLayout.show(screenPanel, CARD_GAME);
                requestFocusInWindow();
            }
        });

        pauseBackToMenuButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paused = false;
                if (pauseOverlayPanel != null) {
                    pauseOverlayPanel.setVisible(false);
                }
                stopAllTimers();
                if (onBack != null) {
                    onBack.run();
                }
            }
        });

        playAgainButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                difficulty = selectedDifficulty;
                resetGame();
                screenLayout.show(screenPanel, CARD_GAME);
                requestFocusInWindow();
            }
        });

        changeDifficultyButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAllTimers();
                showPreStartScreen();
            }
        });

        backToMenuButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAllTimers();
                if (onBack != null) {
                    onBack.run();
                }
            }
        });
    }

    /** Implents keyboard support */
    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        for (char c = 'A'; c <= 'Z'; c++) {
            final char keyChar = c;

            inputMap.put(KeyStroke.getKeyStroke(Character.toLowerCase(c)), "type_lower_" + c);
            inputMap.put(KeyStroke.getKeyStroke(c), "type_upper_" + c);

            actionMap.put("type_lower_" + c, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleTypedLetter(keyChar);
                }
            });

            actionMap.put("type_upper_" + c, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleTypedLetter(keyChar);
                }
            });
        }

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removeLast");
        actionMap.put("removeLast", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeLastLetter();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "clearAll");
        actionMap.put("clearAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAttempt();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escapeKey");
        actionMap.put("escapeKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEscapeKey();
            }
        });
    }

    /** Escape Key checking for pausing game */
    private void handleEscapeKey() {
        if (paused) {
            resumeGame();
            return;
        }
        if (gamePanel.isVisible() && !gameOver) {
            pauseGame();
            return;
        }
        if (preStartPanel.isVisible()) {
            stopAllTimers();
            if (onBack != null) {
                onBack.run();
            }
        }
    }

    private void createTimers() {
        gameTimer = new Timer(100, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onGameTick();
            }
        });
    }

    /**
     * Formats main title
     * 
     * @param text Title text
     * @return A String with formating
     */
    private JLabel createTopLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 30));
        label.setForeground(TEXT);
        return label;
    }

    /** 
     * Formats lives counter
     * 
     * @param lives Number of lives
     * @return A HTML formatted String representing the remaining lives
    */
    private static String formatLivesHtml(int lives) {
        if (lives <= 0) {
            return "<html><span style='color:#222;font-size:34pt;font-weight:bold;'>Lives </span>"
                    + "<span style='color:#999;font-size:30pt;'>—</span></html>";
        }
        if (lives > 5) {
            return "<html><span style='color:#222;font-size:34pt;font-weight:bold;'>Lives </span>"
                    + "<span style='color:#c62828;font-size:48pt;'>&#9829;</span> "
                    + "<span style='color:#222;font-size:38pt;font-weight:bold;'>&times; " + lives + "</span></html>";
        }
        StringBuilder sb = new StringBuilder(
                "<html><span style='color:#222;font-size:34pt;font-weight:bold;'>Lives </span>");
        for (int i = 0; i < lives; i++) {
            sb.append("<span style='color:#c62828;font-size:48pt;'>&#9829;</span>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Formats powerups for display
     * 
     * @return A HTML formatted String representing the held power ups
     */
    private String formatPowerUpsLabelHtml() {
        if (!GameSettings.load().isPowerUpsEnabled()) {
            return "<html><div style='text-align:center;max-width:520px;margin:0 auto;line-height:1.45;'>"
                    + "<span style='font-size:22pt;font-weight:bold;color:#555;'>Power-ups: Off</span><br>"
                    + "<span style='font-size:16pt;color:#777;'>Bonus rounds are disabled</span>"
                    + "</div></html>";
        }

        StringBuilder sb = new StringBuilder(
                "<html><div style='text-align:center;max-width:520px;margin:0 auto;line-height:1.45;"
                        + "font-size:19pt;font-weight:bold;color:#5d3d12;'>");

        sb.append("<span style='font-size:17pt;color:#4e342e;'>Active slow timers</span><br>");
        boolean anySlow = false;
        int slowNum = 0;
        for (PowerUp p : activePowerUps) {
            if (p instanceof SlowTimerPowerUp) {
                anySlow = true;
                slowNum++;
                int slowSec = (int) Math.ceil(Math.max(0.0, p.remainingSeconds) - 1e-9);
                if (slowSec < 0) {
                    slowSec = 0;
                }
                sb.append("<span style='font-size:21pt;'>&#9201; Slow ")
                        .append(slowNum)
                        .append(": ")
                        .append(String.format(Locale.US, "%ds", slowSec))
                        .append("</span><br>");
            }
        }
        if (!anySlow) {
            sb.append("<span style='font-size:20pt;color:#888;'>—</span><br>");
        }

        sb.append("<span style='font-size:17pt;color:#4e342e;'>Bonus round</span><br>");
        if (powerUpChanceThisWord) {
            String reward = pendingBonusIsSlowTimer
                    ? "<span style='color:#0d47a1;'>&#9201; Slow timer</span> (8s)"
                    : "<span style='color:#c62828;'>&#9829; +1 life</span> (instant)";
            sb.append("<span style='font-size:20pt;color:#333;'>Solve <b>this word</b> correctly to earn:<br>")
                    .append(reward)
                    .append("</span><br>");
            sb.append("<span style='font-size:20pt;color:#b71c1c;'>Wrong or time out → bonus skipped, next chance in 1–3 words.</span>");
        } else {
            int n = wordsUntilNextPowerUp;
            if (n == 1) {
                sb.append("<span style='font-size:20pt;color:#0d47a1;white-space:nowrap;'>")
                        .append("Next word: bonus round — reward shown when it starts.</span>");
            } else {
                sb.append("<span style='font-size:20pt;color:#333;'>")
                        .append("<b>")
                        .append(n)
                        .append("</b> more words until the next bonus round.</span>");
            }
        }

        sb.append("</div></html>");
        return sb.toString();
    }

    /**
     * Menu button formating
     * 
     * @param text Label for the button
     * @return A {@link javax.swing.JButton} formatted
     */
    private JButton createMenuButton(String text) {
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

    /** Sets the screen to the pre-game layout */
    private void showPreStartScreen() {
        stopAllTimers();
        clearWordUI();

        gameOver = false;
        acceptingInput = false;
        countdownRunning = false;
        betweenWords = false;
        activePowerUps.clear();
        paused = false;
        if (pauseOverlayPanel != null) {
            pauseOverlayPanel.setVisible(false);
        }
        unsolvedWords.clear();
        currentWord = "";
        currentAttempt.setLength(0);
        hintLabel.setText(" ");
        powerUpChanceThisWord = false;
        clearDefinitionSection();
        lastPowerUpHtmlCache = null;

        difficulty = normalizeDifficulty(selectedDifficulty);
        preStartUserLabel.setText("Player: " + (currentUsername == null || currentUsername.trim().isEmpty() ? "PLAYER" : currentUsername));
        updateDifficultySelectionUI();

        screenLayout.show(screenPanel, CARD_PRESTART);
        requestFocusInWindow();
    }

    /** Sets difficulty for game */
    private void updateDifficultySelectionUI() {
        selectedDifficultyLabel.setText("Selected Difficulty: " + selectedDifficulty);

        easyButton.setEnabled(!"EASY".equals(selectedDifficulty));
        mediumButton.setEnabled(!"MEDIUM".equals(selectedDifficulty));
        hardButton.setEnabled(!"HARD".equals(selectedDifficulty));
    }

    /** Resets the game back to the begging state */
    private void resetGame() {
        stopAllTimers();

        score = 0;
        lives = GameSettings.load().getStartingLives();
        totalAttempts = 0;
        correctAttempts = 0;
        gameOver = false;
        acceptingInput = false;
        countdownRunning = false;
        betweenWords = false;
        activePowerUps.clear();
        paused = false;
        if (pauseOverlayPanel != null) {
            pauseOverlayPanel.setVisible(false);
        }
        unsolvedWords.clear();
        wordsUntilNextPowerUp = 1 + random.nextInt(3);
        powerUpChanceThisWord = false;
        clearDefinitionSection();
        lastPowerUpHtmlCache = null;

        currentWord = "";
        currentAttempt = new StringBuilder();
        timeLimit = getBaseTimeLimit();
        timeRemaining = timeLimit;
        gameStartTimeMillis = System.currentTimeMillis();
        lastServedWord = null;
        applyHintText(" ");

        resetWordPool();
        clearWordUI();

        feedbackLabel.setForeground(ACCENT);
        feedbackLabel.setText("Get ready...");
        countdownLabel.setText(" ");
        countdownLabel.setVisible(false);
        updateLabels();

        clearButton.setEnabled(false);
        removeButton.setEnabled(false);
        if (pauseButton != null) {
            pauseButton.setEnabled(false);
        }

        startCountdown();
    }

    /** Timer used in game rounds */
    private void startCountdown() {
        countdownRunning = true;
        final int[] count = {3};

        countdownLabel.setVisible(true);
        countdownLabel.setText(String.valueOf(count[0]));

        countdownTimer = new Timer(1000, null);
        countdownTimer.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                count[0]--;

                if (count[0] > 0) {
                    countdownLabel.setText(String.valueOf(count[0]));
                } else if (count[0] == 0) {
                    countdownLabel.setText("GO!");
                    feedbackLabel.setText("Start spelling!");
                } else {
                    countdownTimer.stop();
                    countdownRunning = false;
                    countdownLabel.setText(" ");
                    countdownLabel.setVisible(false);
                    loadNextWord();
                    acceptingInput = true;
                    clearButton.setEnabled(true);
                    removeButton.setEnabled(true);
                    updatePauseButtonState();
                    gameTimer.start();
                    requestFocusInWindow();
                }
            }
        });
        countdownTimer.setInitialDelay(1000);
        countdownTimer.start();
    }

    /** Primary game loop logic */
    private void onGameTick() {
        if (paused || gameOver || !acceptingInput || countdownRunning || betweenWords) {
            return;
        }

        boolean anySlowActive = false;
        for (PowerUp p : activePowerUps) {
            if (p instanceof SlowTimerPowerUp && p.remainingSeconds > 0.0) {
                anySlowActive = true;
                break;
            }
        }
        double decay = anySlowActive ? 0.05 : 0.1;

        timeRemaining -= decay;
        if (timeRemaining < 0.0) {
            timeRemaining = 0.0;
        }

        Iterator<PowerUp> powerIt = activePowerUps.iterator();
        boolean anyExpired = false;
        while (powerIt.hasNext()) {
            PowerUp p = powerIt.next();
            p.remainingSeconds -= 0.1;
            if (p.remainingSeconds <= 0.0) {
                p.expire(this);
                powerIt.remove();
                anyExpired = true;
            }
        }
        if (anyExpired) {
            feedbackLabel.setForeground(ACCENT);
            feedbackLabel.setText("Power-up expired.");
        }

        updateLabels();

        if (timeRemaining <= 0.0) {
            handleTimeout();
        }
    }

    /**
     * Reveals a scrambled or partial word to the player
     * 
     * @param word The current word the player is trying to spell
     * @return A String containing the hint
     */
    private String buildHintText(String word) {
        if (word == null || word.isEmpty()) {
            return " ";
        }

        String upperWord = word.toUpperCase(Locale.ENGLISH);

        if ("EASY".equals(difficulty)) {
            return " ";
        }

        if ("MEDIUM".equals(difficulty)) {
            int prefixLen = Math.min(2, upperWord.length());
            return "Hint: starts with " + upperWord.substring(0, prefixLen);
        }

        if ("HARD".equals(difficulty)) {
            if (upperWord.length() <= 2) {
                return "Hint: starts with " + upperWord;
            }
            int prefixLen = Math.min(2, upperWord.length());
            int suffixLen = Math.min(2, upperWord.length());
            String prefix = upperWord.substring(0, prefixLen);
            String suffix = upperWord.substring(upperWord.length() - suffixLen);
            return "Hint: starts with " + prefix + " and ends with " + suffix;
        }

        return " ";
    }

    /**
     * Updates the hint text based on the current word
     * 
     * @param plain The hint String
     */
    private void applyHintText(String plain) {
        if (plain == null) {
            hintLabel.setText(" ");
            return;
        }
        String trimmed = plain.trim();
        if (trimmed.isEmpty()) {
            hintLabel.setText(" ");
            return;
        }
        String esc = escapeHtml(trimmed);
        hintLabel.setText(
                "<html><div style='text-align:center;max-width:900px;margin:0 auto;padding:12px 8px 16px 8px;"
                        + "font-weight:bold;font-size:18pt;font-family:sans-serif;color:rgb(25,55,130);"
                        + "line-height:1.35;display:block;'>"
                        + esc + "</div></html>");
    }

    /** Gets the next word for the game */
    private void loadNextWord() {
        currentWord = getNextUniqueWord();
        currentAttempt.setLength(0);
        timeLimit = getBaseTimeLimit();
        timeRemaining = timeLimit;

        buildSlots(currentWord);
        buildLetters(currentWord);
        applyHintText(buildHintText(currentWord));

        clearDefinitionSection();
        lastPowerUpHtmlCache = null;
        feedbackLabel.setForeground(ACCENT);
        feedbackLabel.setText(" ");

        if (GameSettings.load().isPowerUpsEnabled() && wordsUntilNextPowerUp == 0) {
            powerUpChanceThisWord = true;
            pendingBonusIsSlowTimer = random.nextBoolean();
        } else {
            powerUpChanceThisWord = false;
        }

        updateLabels();
        revalidate();
        repaint();
        requestFocusInWindow();
    }

    /**
     * Build the character slots for the letters in the game
     * 
     * @param word A String with the letter
     */
    private void buildSlots(String word) {
        slotsPanel.removeAll();
        slotLabels.clear();

        for (int i = 0; i < word.length(); i++) {
            JLabel slot = new JLabel("_", SwingConstants.CENTER);
            slot.setFont(new Font("Arial", Font.BOLD, 48));
            slot.setForeground(TEXT);
            slot.setPreferredSize(new Dimension(56, 64));
            slotLabels.add(slot);
            slotsPanel.add(slot);
        }

        slotsPanel.revalidate();
        slotsPanel.repaint();
    }

    /**
     * Builds the letters for the game
     * 
     * @param word A String with letter
    */
    private void buildLetters(String word) {
        lettersPanel.removeAll();
        letterButtons.clear();
        pickSequence.clear();

        List<String> letters = new ArrayList<String>();
        for (int i = 0; i < word.length(); i++) {
            letters.add(String.valueOf(Character.toUpperCase(word.charAt(i))));
        }
        Collections.shuffle(letters, random);

        for (String letter : letters) {
            LetterCircleButton button = new LetterCircleButton(letter);
            button.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toggleLetter(button);
                }
            });
            letterButtons.add(button);
            lettersPanel.add(button);
        }

        lettersPanel.revalidate();
        lettersPanel.repaint();
    }

    /** Resets the letters after attempt */
    private void rebuildAttemptFromPickSequence() {
        currentAttempt.setLength(0);
        for (LetterCircleButton b : pickSequence) {
            currentAttempt.append(b.getText().charAt(0));
        }
    }

    /** Changes the selection state of the letter buttons to prevent reuse
     * 
     * @param button The {@link LetterCircleButton} to be toggled
    */
    private void toggleLetter(LetterCircleButton button) {
        if (paused || !acceptingInput || button == null || gameOver || countdownRunning || betweenWords) {
            return;
        }

        if (button.isPicked()) {
            pickSequence.remove(button);
            button.setPicked(false);
        } else {
            if (pickSequence.size() >= currentWord.length()) {
                return;
            }
            pickSequence.add(button);
            button.setPicked(true);
        }

        rebuildAttemptFromPickSequence();
        updateSlots();
        feedbackLabel.setForeground(ACCENT);
        feedbackLabel.setText(" ");
        requestFocusInWindow();

        if (currentAttempt.length() == currentWord.length()) {
            checkAttempt();
        }
    }

    /**
     * Toggles button if there letter is type through keyboard
     * 
     * @param typedChar The letter that was typed
     */
    private void handleTypedLetter(char typedChar) {
        if (paused || !acceptingInput || gameOver || countdownRunning || betweenWords) {
            return;
        }

        char target = Character.toUpperCase(typedChar);

        for (LetterCircleButton button : letterButtons) {
            if (!button.isPicked() && button.getText().charAt(0) == target) {
                toggleLetter(button);
                break;
            }
        }
    }

    /** Removes the previous letter that was added */
    private void removeLastLetter() {
        if (paused || !acceptingInput || pickSequence.isEmpty()) {
            return;
        }

        LetterCircleButton last = pickSequence.remove(pickSequence.size() - 1);
        last.setPicked(false);
        rebuildAttemptFromPickSequence();

        updateSlots();
        feedbackLabel.setForeground(ACCENT);
        feedbackLabel.setText("Removed last letter.");
        requestFocusInWindow();
    }

    /** Removes all letters after correct answer */
    private void clearAttempt() {
        if (paused || !acceptingInput) {
            return;
        }

        pickSequence.clear();
        for (LetterCircleButton button : letterButtons) {
            button.setPicked(false);
        }
        currentAttempt.setLength(0);

        updateSlots();
        feedbackLabel.setForeground(ACCENT);
        feedbackLabel.setText("Cleared selection.");
        requestFocusInWindow();
    }

    /** Updates the visual state of the slots to show where the next letter will be added */
    private void updateSlots() {
        for (int i = 0; i < slotLabels.size(); i++) {
            if (i < currentAttempt.length()) {
                slotLabels.get(i).setText(String.valueOf(currentAttempt.charAt(i)));
            } else {
                slotLabels.get(i).setText("_");
            }
        }
    }

    /** Verifies if the word was spelt correctly */
    private void checkAttempt() {
        totalAttempts++;
        acceptingInput = false;
        betweenWords = true;

        String built = currentAttempt.toString();
        String builtLower = built.toLowerCase(Locale.ENGLISH);
        boolean exact = built.equalsIgnoreCase(currentWord);
        boolean validAnagram = !exact
                && validationWords.contains(builtLower)
                && isAnagramOf(built, currentWord);

        if (exact || validAnagram) {
            correctAttempts++;

            int points = getPointsForDifficulty();
            score += points;

            feedbackLabel.setForeground(SUCCESS);
            if (validAnagram) {
                feedbackLabel.setText("Correct! (Valid anagram) The word was " + currentWord + ".");
            } else {
                feedbackLabel.setText("Correct! The word was " + currentWord + ".");
            }
            onWordCompleted(true);
            updateLabels();
            scheduleNextWord(900);
        } else {
            lives--;
            unsolvedWords.add(currentWord.toLowerCase(Locale.ENGLISH));
            onWordCompleted(false);

            final String mistakeWord = currentWord;
            final boolean willEndGame = lives <= 0;
            showWrongAnswerFeedback(
                    "Incorrect. Correct word: " + currentWord + ".",
                    mistakeWord,
                    () -> {
                        if (willEndGame) {
                            endGame();
                        } else {
                            scheduleNextWord(0);
                        }
                    });
            updateLabels();
        }
    }

    /** Logic for when game timer reaches zero */
    private void handleTimeout() {
        if (gameOver) {
            return;
        }

        acceptingInput = false;
        betweenWords = true;
        lives--;
        unsolvedWords.add(currentWord.toLowerCase(Locale.ENGLISH));
        onWordCompleted(false);

        final String mistakeWord = currentWord;
        final boolean willEndGame = lives <= 0;
        showWrongAnswerFeedback(
                "Time ran out. Correct word: " + currentWord + ".",
                mistakeWord,
                () -> {
                    if (willEndGame) {
                        endGame();
                    } else {
                        scheduleNextWord(0);
                    }
                });
        updateLabels();
    }

    /**
     * Checks for anagrams in game
     * 
     * @param guess The player's inputed word
     * @param target The word the player has to guess
     */
    private static boolean isAnagramOf(String guess, String target) {
        if (guess == null || target == null || guess.length() != target.length()) {
            return false;
        }
        char[] a = guess.toLowerCase(Locale.ENGLISH).toCharArray();
        char[] b = target.toLowerCase(Locale.ENGLISH).toCharArray();
        Arrays.sort(a);
        Arrays.sort(b);
        return Arrays.equals(a, b);
    }

    /** Positions game feedback */
    private void scrollFeedbackToTop() {
        if (feedbackScrollPane != null) {
            SwingUtilities.invokeLater(() -> {
                feedbackScrollPane.getVerticalScrollBar().setValue(0);
                feedbackScrollPane.revalidate();
            });
        }
    }

    /** Positions word definition */
    private void scrollDefinitionToTop() {
        if (definitionScrollPane != null) {
            SwingUtilities.invokeLater(() -> {
                definitionScrollPane.getVerticalScrollBar().setValue(0);
                definitionScrollPane.revalidate();
            });
        }
    }

    /** Clears text for definition and resets position */
    private void clearDefinitionSection() {
        if (definitionLabel != null) {
            definitionLabel.setText(" ");
        }
        if (definitionSectionPanel != null) {
            definitionSectionPanel.setVisible(false);
        }
        if (definitionScrollPane != null) {
            SwingUtilities.invokeLater(() -> {
                if (definitionScrollPane != null) {
                    definitionScrollPane.getVerticalScrollBar().setValue(0);
                }
            });
        }
    }

    /** Displays the feedback after missed word
     * 
     * @param firstLine Header text to be displayed
     * @param lookupWord The target word to be defined
     * @param afterReading A {@link Runnable} containing the logic for continuing or ending the game
     */
    private void showWrongAnswerFeedback(String firstLine, final String lookupWord, final Runnable afterReading) {
        clearDefinitionSection();
        feedbackLabel.setForeground(ERROR);
        feedbackLabel.setText(firstLine);
        scrollFeedbackToTop();
        final String frozenWord = lookupWord;
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return DefinitionLookup.fetchDefinition(frozenWord);
            }

            @Override
            protected void done() {
                try {
                    String def = get();
                    if (def != null && !def.trim().isEmpty() && frozenWord.equalsIgnoreCase(currentWord)) {
                        String d = truncateDefinitionForDisplay(def.trim());
                        if (definitionLabel != null) {
                            definitionLabel.setText(
                                    "<html><div style='text-align:center;width:520px;font-size:17pt;line-height:1.35;'>"
                                            + escapeHtml(d)
                                            + "</div></html>");
                        }
                        if (definitionSectionPanel != null) {
                            definitionSectionPanel.setVisible(true);
                        }
                        scrollDefinitionToTop();
                    }
                } catch (Exception ignored) {
                }
                Timer readPause = new Timer(DEFINITION_READ_DELAY_MS, e -> {
                    ((Timer) e.getSource()).stop();
                    afterReading.run();
                });
                readPause.setRepeats(false);
                readPause.start();
            }
        };
        worker.execute();
    }

    /** 
     * Schedules next word after provided delay
     * 
     * @param delayMillis The delay in milliseconds
     */
    private void scheduleNextWord(int delayMillis) {
        if (wordTransitionTimer != null) {
            wordTransitionTimer.stop();
        }
        wordTransitionTimer = new Timer(delayMillis, null);
        wordTransitionTimer.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wordTransitionTimer.stop();
                wordTransitionTimer = null;
                betweenWords = false;
                loadNextWord();
                acceptingInput = true;
            }
        });
        wordTransitionTimer.setRepeats(false);
        wordTransitionTimer.start();
    }

    /**
     * Handles transition after a round concludes
     * 
     * @param solvedCorrectly A bool, true if target word was found
     */
    private void onWordCompleted(boolean solvedCorrectly) {
        if (!GameSettings.load().isPowerUpsEnabled()) {
            return;
        }
        if (powerUpChanceThisWord) {
            if (solvedCorrectly) {
                activatePendingPowerUp(true);
            }
            wordsUntilNextPowerUp = 1 + random.nextInt(3);
            powerUpChanceThisWord = false;
        } else {
            if (wordsUntilNextPowerUp > 0) {
                wordsUntilNextPowerUp--;
            }
        }
    }

    /**
     * Activates the power-up currently help in the pending slot
     * 
     * @param silentFeedback A bool to toggle if powerup use is annouced
     */
    private void activatePendingPowerUp(boolean silentFeedback) {
        if (!GameSettings.load().isPowerUpsEnabled()) {
            return;
        }
        if (pendingBonusIsSlowTimer) {
            SlowTimerPowerUp p = new SlowTimerPowerUp();
            activePowerUps.add(p);
            if (!silentFeedback) {
                p.activate(this);
            }
        } else {
            int cap = GameSettings.load().getMaxTotalLives();
            lives = Math.min(cap, lives + 1);
            if (!silentFeedback) {
                feedbackLabel.setForeground(SUCCESS);
                feedbackLabel.setText("Power-Up: +1 Life!");
            }
        }
        updateLabels();
    }

    /** Clears the word UI from the screen */
    private void clearWordUI() {
        slotsPanel.removeAll();
        lettersPanel.removeAll();
        slotLabels.clear();
        letterButtons.clear();
        slotsPanel.revalidate();
        slotsPanel.repaint();
        lettersPanel.revalidate();
        lettersPanel.repaint();
    }

    /**
     * Standardizes the difficulty String format
     * 
     * @param value The String to be formatted
     */
    private String normalizeDifficulty(String value) {
        if (value == null) {
            return "MEDIUM";
        }

        String normalized = value.trim().toUpperCase(Locale.ENGLISH);
        if ("EASY".equals(normalized) || "MEDIUM".equals(normalized) || "HARD".equals(normalized)) {
            return normalized;
        }
        return "MEDIUM";
    }

    /** Gets time limit based on difficulty */
    private double getBaseTimeLimit() {
        if ("EASY".equals(difficulty)) {
            return 12.0;
        }
        if ("HARD".equals(difficulty)) {
            return 7.0;
        }
        return 9.0;
    }

    /** Gets points based on difficulty */
    private int getPointsForDifficulty() {
        if ("EASY".equals(difficulty)) {
            return 10;
        }
        if ("HARD".equals(difficulty)) {
            return 20;
        }
        return 15;
    }

    /** Gets word based on difficulty */
    private List<String> getWordsForDifficulty() {
        if ("EASY".equals(difficulty)) {
            return easyWords;
        }
        if ("HARD".equals(difficulty)) {
            return hardWords;
        }
        return mediumWords;
    }

    /** Adds the previously picked words back into the pool */
    private void resetWordPool() {
        remainingWords.clear();
        remainingWords.addAll(getWordsForDifficulty());
        Collections.shuffle(remainingWords, random);

        if (remainingWords.size() > 1 && lastServedWord != null
                && lastServedWord.equalsIgnoreCase(remainingWords.get(0))) {
            Collections.rotate(remainingWords, 1);
        }
    }

    /** Gets the next word */
    private String getNextUniqueWord() {
        if (remainingWords.isEmpty()) {
            resetWordPool();
        }

        String next = remainingWords.remove(0);

        if (lastServedWord != null && next.equalsIgnoreCase(lastServedWord) && !remainingWords.isEmpty()) {
            remainingWords.add(next);
            next = remainingWords.remove(0);
        }

        lastServedWord = next;
        return next;
    }

    /** Refreshes game labels to match the current state */
    private void updateLabels() {
        String displayName = (currentUsername == null || currentUsername.trim().isEmpty())
                ? "PLAYER"
                : currentUsername.toUpperCase(Locale.ENGLISH);

        titleLabel.setText("DEATH BY SPELL CHECK - " + displayName);
        difficultyLabel.setText("Difficulty: " + difficulty);
        scoreLabel.setText("Score: " + score);
        livesLabel.setText(formatLivesHtml(lives));

        timerValueLabel.setText(String.format(Locale.US, "%.1f", Math.max(0.0, timeRemaining)));
        if (timeRemaining <= 3.0) {
            timerValueLabel.setForeground(ERROR);
        } else if (timeRemaining <= 5.0) {
            timerValueLabel.setForeground(WARNING);
        } else {
            timerValueLabel.setForeground(TEXT);
        }

        String powerHtml = formatPowerUpsLabelHtml();
        if (lastPowerUpHtmlCache == null || !powerHtml.equals(lastPowerUpHtmlCache)) {
            powerUpValueLabel.setText(powerHtml);
            lastPowerUpHtmlCache = powerHtml;
        }
        updatePauseButtonState();
    }

    /** Ends the current gameplay session and transitions to the results screen */
    private void endGame() {
        if (gameOver) {
            return;
        }

        gameOver = true;
        acceptingInput = false;
        betweenWords = false;
        stopAllTimers();

        clearButton.setEnabled(false);
        removeButton.setEnabled(false);
        if (pauseButton != null) {
            pauseButton.setEnabled(false);
        }
        paused = false;
        if (pauseOverlayPanel != null) {
            pauseOverlayPanel.setVisible(false);
        }

        double accuracy = calculateAccuracy();
        int wpm = calculateWpmForRecordOnly();

        SessionRecord record = new SessionRecord(
                currentUsername,
                difficulty,
                score,
                accuracy,
                wpm,
                LocalDateTime.now().toString()
        );

        sessions.add(record);

        try {
            SessionRecord.appendSessionToFile(record);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        gameOverStatsLabel.setText(
                "<html><div style='text-align:center;'>"
                        + "Player: " + escapeHtml(currentUsername) + "<br><br>"
                        + "Difficulty: " + escapeHtml(difficulty) + "<br>"
                        + "Score: " + score + "<br>"
                        + "WPM: " + wpm + "<br>"
                        + "Accuracy: " + String.format(Locale.US, "%.2f%%", accuracy)
                        + "</div></html>"
        );

        if (gameOverWordsPane != null) {
            gameOverWordsPane.setText(
                    "<html><body style='font-family:sans-serif;color:#333;'>"
                            + "<p style='text-align:center;'><i>Loading definitions…</i></p></body></html>");
            final List<String> wordsSnapshot = new ArrayList<String>(unsolvedWords);
            SwingWorker<String, Void> wordsWorker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><body style='font-family:sans-serif;color:#333;'>");
                    if (wordsSnapshot.isEmpty()) {
                        sb.append("<p style='text-align:center;'><i>No unsolved words to show.</i></p>");
                    } else {
                        sb.append("<p style='font-weight:bold;font-size:12pt;text-align:center;'>Words you did not solve</p>");
                        for (String w : wordsSnapshot) {
                            String def = DefinitionLookup.fetchDefinition(w);
                            String d = def != null && !def.trim().isEmpty()
                                    ? truncateDefinitionForDisplay(def.trim())
                                    : "—";
                            sb.append("<p style='margin:8px 4px;'><b>")
                                    .append(escapeHtml(w))
                                    .append("</b><br>")
                                    .append(escapeHtml(d))
                                    .append("</p>");
                        }
                    }
                    sb.append("</body></html>");
                    return sb.toString();
                }

                @Override
                protected void done() {
                    try {
                        gameOverWordsPane.setText(get());
                    } catch (Exception ignored) {
                    }
                }
            };
            wordsWorker.execute();
        }

        screenLayout.show(screenPanel, CARD_GAMEOVER);
        revalidate();
        repaint();
        requestFocusInWindow();
    }

    /** Finds the player's word accuracy */
    private double calculateAccuracy() {
        if (totalAttempts == 0) {
            return 0.0;
        }
        return (correctAttempts * 100.0) / totalAttempts;
    }

    /** Finds the player's word typing speed in minutes */
    private int calculateWpmForRecordOnly() {
        long elapsedMillis = Math.max(1L, System.currentTimeMillis() - gameStartTimeMillis);
        double minutes = elapsedMillis / 60000.0;
        double wordsEquivalent = Math.max(0, correctAttempts);
        return (int) Math.round(wordsEquivalent / minutes);
    }

    /** Stops all gamplay timers */
    private void stopAllTimers() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        if (wordTransitionTimer != null) {
            wordTransitionTimer.stop();
            wordTransitionTimer = null;
        }
    }

    /** Checks if pausing won't terminate a ongoing function */
    private boolean canPauseNow() {
        return gamePanel.isVisible() && !gameOver && !paused
                && acceptingInput && !betweenWords && !countdownRunning;
    }

    /** Changes game pause state */
    private void updatePauseButtonState() {
        if (pauseButton != null) {
            pauseButton.setEnabled(canPauseNow());
        }
    }

    /** Pauses the game */
    private void pauseGame() {
        if (pauseOverlayPanel == null || !canPauseNow()) {
            return;
        }
        paused = true;
        gameTimerWasRunning = gameTimer != null && gameTimer.isRunning();
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        pauseOverlayPanel.setVisible(true);
        revalidate();
        repaint();
        requestFocusInWindow();
    }

    /** Resumes the game */
    private void resumeGame() {
        if (!paused) {
            return;
        }
        paused = false;
        if (pauseOverlayPanel != null) {
            pauseOverlayPanel.setVisible(false);
        }
        if (!gameOver && gameTimerWasRunning && gameTimer != null) {
            gameTimer.start();
        }
        revalidate();
        repaint();
        requestFocusInWindow();
    }

    /** Loads words to be used in gameplay from Dictionary*/
    private void loadAllWordFiles() {
        try {
            WordDictionary.ensureDictionaryFile();
            Path path = WordDictionary.getDictionaryPath();
            WordDictionary.DictionaryBuckets buckets = WordDictionary.loadBucketsFromFile(path);
            easyWords = new ArrayList<String>(buckets.easy);
            mediumWords = new ArrayList<String>(buckets.medium);
            hardWords = new ArrayList<String>(buckets.hard);
            validationWords = WordDictionary.loadAllAcceptableWords(path);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (easyWords.isEmpty()) {
            easyWords = new ArrayList<String>(Arrays.asList(
                    "cat", "dog", "sun", "hat", "map", "pen", "cup", "bed", "car", "box"
            ));
        }
        if (mediumWords.isEmpty()) {
            mediumWords = new ArrayList<String>(Arrays.asList(
                    "planet", "silver", "button", "window", "garden", "rocket", "camera", "school"
            ));
        }
        if (hardWords.isEmpty()) {
            hardWords = new ArrayList<String>(Arrays.asList(
                    "reaction", "survival", "accuracy", "velocity", "challenge", "keyboard"
            ));
        }

        easyWords = dedupeWords(easyWords);
        mediumWords = dedupeWords(mediumWords);
        hardWords = dedupeWords(hardWords);

        if (validationWords.isEmpty()) {
            rebuildValidationFromPoolWords();
        }
    }

    /** Rebuilds difficulty word verification sets */
    private void rebuildValidationFromPoolWords() {
        validationWords = new HashSet<String>();
        for (String w : easyWords) {
            if (WordFilter.isAcceptable(w)) {
                validationWords.add(w);
            }
        }
        for (String w : mediumWords) {
            if (WordFilter.isAcceptable(w)) {
                validationWords.add(w);
            }
        }
        for (String w : hardWords) {
            if (WordFilter.isAcceptable(w)) {
                validationWords.add(w);
            }
        }
    }

    /**
     * Removes duplicate words from list
     * 
     * @param words List to be checked for dupluicates
     * @return List without dublicates
     */
    private List<String> dedupeWords(List<String> words) {
        Set<String> unique = new LinkedHashSet<String>();
        for (String word : words) {
            if (word != null) {
                String cleaned = word.trim().toLowerCase(Locale.ENGLISH);
                if (!cleaned.isEmpty()) {
                    unique.add(cleaned);
                }
            }
        }
        return new ArrayList<String>(unique);
    }

    /**
     * Verifies word definition will fit on display
     * 
     * @param text The text to be checked
     * @return Resized definition that fits on the screen
     */
    private static String truncateDefinitionForDisplay(String text) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        if (t.length() <= MAX_DEFINITION_DISPLAY_CHARS) {
            return t;
        }
        int budget = MAX_DEFINITION_DISPLAY_CHARS - 3;
        if (budget <= 0) {
            return "...";
        }
        String chunk = t.substring(0, budget);
        int lastSpace = chunk.lastIndexOf(' ');
        if (lastSpace > budget / 5) {
            chunk = chunk.substring(0, lastSpace);
        }
        return chunk.trim() + "...";
    }

    /**
     * Removes text modifiers for HTML use later
     * 
     * @param text The text to be sanitized
     * @return A String with the sanitized text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private abstract static class PowerUp {
        protected final String name;
        protected double remainingSeconds;

        protected PowerUp(String name, double remainingSeconds) {
            this.name = name;
            this.remainingSeconds = remainingSeconds;
        }

        abstract void activate(GameplayScreen screen);
        abstract void expire(GameplayScreen screen);
    }

    private static class SlowTimerPowerUp extends PowerUp {
        SlowTimerPowerUp() {
            super("Slow Timer", 8.0);
        }

        @Override
        void activate(GameplayScreen screen) {
            screen.feedbackLabel.setForeground(SUCCESS);
            screen.feedbackLabel.setText("Power-Up Activated: Slow Timer");
        }

        @Override
        void expire(GameplayScreen screen) {
        }
    }

    /**
     * Custom text outline in pause screen
     * 
     * @param g2 The graphics context
     * @param text The text to be drawn
     * @param x Horizontal position
     * @param y Verticle polsition
     * @param fill The fill colour
     * @param strokeRadius The thickness of the outline
    */
    private static void drawPauseOutlinedString(Graphics2D g2, String text, int x, int y, Color fill, Color outline, int strokeRadius) {
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

    /** Class for custom pause menu text */
    private static class PauseOutlinedTitleLabel extends JLabel {
        PauseOutlinedTitleLabel(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
            setFont(new Font("SansSerif", Font.BOLD, 42));
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            int w = Math.max(560, d.width);
            int h = Math.max(56, d.height);
            return new Dimension(w, h);
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
            drawPauseOutlinedString(g2, t, x, y, Color.WHITE, Color.BLACK, 2);
            g2.dispose();
        }
    }

    /** Custom graphical element */
    private static class TemplatePanel extends JPanel {
        private static final double BASE_W = 1920.0;
        private static final double BASE_H = 1080.0;

        TemplatePanel() {
            setOpaque(true);
            setBackground(BG);
        }

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

            g2.setColor(BG);
            g2.fillRect(0, 0, w, h);

            g2.setColor(Color.BLACK);

            fillPath(g2, sx, sy,
                    1473,474, 1441,427, 1371,457, 1248,256, 1042,355, 931,281,
                    968,182, 1053,145, 1070,179, 1103,167, 1107,137, 1150,139,
                    1161,86, 1090,55, 911,127, 816,278, 778,244, 693,242,
                    653,286, 659,335, 732,379, 648,439, 557,365, 557,314,
                    519,299, 502,334, 451,335, 446,390, 509,414, 595,506,
                    605,491, 529,406, 466,377, 470,351, 513,355, 537,326,
                    538,374, 650,461, 771,375, 678,329, 672,293, 703,260,
                    774,263, 821,306, 925,142, 1088,75, 1137,96, 1135,118,
                    1090,116, 1080,155, 1063,120, 953,166, 909,289, 1042,376,
                    1241,280, 1360,480, 1433,451, 1432,497
            );

            fillPath(g2, sx, sy,
                    1311,486, 1224,345, 1044,452, 1055,483, 1068,482,
                    1073,478, 1068,459, 1217,370, 1295,493
            );

            fillPath(g2, sx, sy,
                    704,479, 709,491, 714,494, 794,446,
                    895,492, 902,490, 906,476, 791,425
            );

            fillPath(g2, sx, sy,
                    993,584, 1037,683, 1256,725, 1268,812, 1319,818, 1353,657,
                    1117,597, 1107,575, 1091,581, 1104,614, 1333,674, 1304,798,
                    1287,798, 1273,708, 1052,667, 1010,580
            );

            g2.dispose();
        }

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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<SessionRecord> localSessions = new ArrayList<SessionRecord>();

                Runnable testOnBack = () -> {
                    JOptionPane.showMessageDialog(null, "Back to menu requested.");
                };
 
                GameplayScreen gameplayScreen = new GameplayScreen(testOnBack);
                gameplayScreen.setData("SAHA", localSessions, "MEDIUM");

                JFrame frame = new JFrame("My Game");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1400, 900);
                frame.setMinimumSize(new Dimension(1200, 800));
                frame.setLocationRelativeTo(null);
                frame.setContentPane(gameplayScreen);
                frame.setVisible(true);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        gameplayScreen.requestFocusInWindow();
                    }
                });
            }
        });
    }
}