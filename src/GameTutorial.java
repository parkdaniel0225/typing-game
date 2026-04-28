import java.awt.*;
import java.awt.geom.GeneralPath;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the tutorial that explains how to play the game
 * 
 * @author Derik Koustrup
 */

public class GameTutorial extends JPanel {

    private static final double BASE_W = 1920.0;
    private static final double BASE_H = 1080.0;

    private final String targetWord = "THE";
    private List<LetterWithUnderline> underlines = new ArrayList<>();
    private final List<LetterCircleButton> letterPickOrder = new ArrayList<>();
    private LetterCircleButton[] circleButtons;

    /** GameTutorial Constructor */
    public GameTutorial() {
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                String typedLetter = String.valueOf(e.getKeyChar()).toUpperCase();


                if ("THE".contains(typedLetter)) {
                    handleLetterClick(typedLetter);
                }
            }
        });


        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                requestFocusInWindow();
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(new Color(235, 235, 235));

        JLabel instructions = new JLabel("<html><center>Click the letters at the bottom<br>" +
                " of the screen or type them in, to <br>" +
                "fill in the slots with the correct <br>" +
                "word before the timer runs out</center></html>", SwingConstants.CENTER);
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructions.setFont(new Font("SansSerif", Font.BOLD, 35));


        JPanel combinedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        combinedPanel.setOpaque(false);

        underlines.add(new LetterWithUnderline(""));
        underlines.add(new LetterWithUnderline(""));
        underlines.add(new LetterWithUnderline(""));

        for (LetterWithUnderline lu : underlines) combinedPanel.add(lu);


        JPanel circlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        circlePanel.setOpaque(false);

        LetterCircleButton c1 = new LetterCircleButton("H");
        LetterCircleButton c2 = new LetterCircleButton("T");
        LetterCircleButton c3 = new LetterCircleButton("E");
        circleButtons = new LetterCircleButton[] { c1, c2, c3 };

        c1.addActionListener(e -> handleButtonClick(c1));
        c2.addActionListener(e -> handleButtonClick(c2));
        c3.addActionListener(e -> handleButtonClick(c3));

        circlePanel.add(c1);
        circlePanel.add(c2);
        circlePanel.add(c3);


        JButton returnBtn = createButton("BACK TO MENU");
        returnBtn.setPreferredSize(new Dimension(320, 55));
        returnBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.dispose();
            }
        });
        returnBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(instructions);
        add(Box.createRigidArea(new Dimension(0, 100)));
        add(combinedPanel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(circlePanel);
        add(Box.createRigidArea(new Dimension(0, 40)));
        add(returnBtn);
        add(Box.createVerticalGlue());
    }

    /**
     * Checks if a button has been pressed
     * 
     * @param b The {@link LetterCircleButton} to be checked
     * @return A bool, true if button has been pressed
     */
    private boolean isLetterPicked(LetterCircleButton b) {
        return b.isPicked();
    }

    /**
     * Changes the pressed status of a button
     * 
     * @param b The {@link LetterCircleButton} to be checked
     * @param picked A bool, true if button has been pressed
     */
    private void setLetterPicked(LetterCircleButton b, boolean picked) {
        b.setPicked(picked);
    }

    /**
     * Logic if button is pressed
     * 
     * @param button The {@link LetterCircleButton} that has been pressed
     */
    private void handleButtonClick(LetterCircleButton button) {
        if (isLetterPicked(button)) {
            applyUnpick(button);
        } else if (letterPickOrder.size() < underlines.size()) {
            applyPick(button);
        }
    }

    /**
     * Logic if a letter has chosen
     * 
     * @param clickedLetter The letter that was chosen
     */
    private void handleLetterClick(String clickedLetter) {
        LetterCircleButton btn = null;
        for (LetterCircleButton b : circleButtons) {
            if (b.getText().equals(clickedLetter)) {
                btn = b;
                break;
            }
        }
        if (btn != null) {
            handleButtonClick(btn);
        }
    }

    /**
     * Updates the tutorial status after a button has been pressed
     * 
     * @param button The {@link LetterCircleButton} that has been pressed
    */
    private void applyPick(LetterCircleButton button) {
        int slot = letterPickOrder.size();
        String clickedLetter = button.getText();
        LetterWithUnderline currentSlot = underlines.get(slot);
        currentSlot.setText(clickedLetter);
        currentSlot.setLetterVisible(true);

        if (clickedLetter.equals(String.valueOf(targetWord.charAt(slot)))) {
            currentSlot.setTextColour(new Color(0, 150, 0));
        } else {
            currentSlot.setTextColour(Color.RED);
        }

        setLetterPicked(button, true);
        letterPickOrder.add(button);

        if (letterPickOrder.size() == underlines.size()) {
            Timer resetTimer = new Timer(1000, e -> resetGame());
            resetTimer.setRepeats(false);
            resetTimer.start();
        }
    }

    /**
     * Reverses the selection of a letter
     * 
     * @param button The {@link LetterCircleButton} that was pressed last
     */
    private void applyUnpick(LetterCircleButton button) {
        int idx = -1;
        for (int i = 0; i < letterPickOrder.size(); i++) {
            if (letterPickOrder.get(i) == button) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            return;
        }
        letterPickOrder.remove(idx);
        setLetterPicked(button, false);

        for (int i = 0; i < underlines.size(); i++) {
            LetterWithUnderline lu = underlines.get(i);
            if (i < letterPickOrder.size()) {
                String ch = letterPickOrder.get(i).getText();
                lu.setText(ch);
                lu.setLetterVisible(true);
                if (ch.equals(String.valueOf(targetWord.charAt(i)))) {
                    lu.setTextColour(new Color(0, 150, 0));
                } else {
                    lu.setTextColour(Color.RED);
                }
            } else {
                lu.setLetterVisible(false);
                lu.setText("");
            }
        }
    }

    /** Resets the tutorial Screen */
    private void resetGame() {
        letterPickOrder.clear();
        for (LetterCircleButton b : circleButtons) {
            setLetterPicked(b, false);
        }
        for (LetterWithUnderline lu : underlines) {
            lu.setLetterVisible(false);
            lu.setText("");
        }
        repaint();
    }

    /** Custom background element */
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

    /** Fills in lines for custom element */
    private static void fillPath(Graphics2D g2, double sx, double sy, int... pts) {
        GeneralPath path = new GeneralPath();
        path.moveTo(pts[0] * sx, pts[1] * sy);
        for (int i = 2; i < pts.length; i += 2) {
            path.lineTo(pts[i] * sx, pts[i + 1] * sy);
        }
        path.closePath();
        g2.fill(path);
    }

    /** Class for letters displayed in tutorial */
    static class LetterWithUnderline extends JPanel {
        private String text;
        private boolean isVisible = false;
        private Color textColour = Color.BLACK;

        /**
         * Constructor for class
         * 
         * @param text Leter to be displayed
        */
        public LetterWithUnderline(String text) {
            this.text = text;
            setPreferredSize(new Dimension(65, 60));
            setOpaque(false);
        }


        public void setText(String newText) { this.text = newText; repaint(); }
        public void setTextColour(Color newColour) { this.textColour = newColour; repaint(); }
        public void setLetterVisible(boolean visible) { this.isVisible = visible; repaint(); }

        /** Drawing element */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(5));
            g2d.drawLine(5, getHeight() - 5, getWidth() - 5, getHeight() - 5);

            if (isVisible) {
                g2d.setColor(textColour);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 50));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() - 20);
            }
        }
    }

    /**
     * Creats button for tutorial
     * 
     * @param text The button text
    */
    private JButton createButton(String text) {
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
                new javax.swing.border.EmptyBorder(8, 12, 8, 12)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        return button;
    }
}
