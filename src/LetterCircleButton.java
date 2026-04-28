import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JButton;

/**
 * Custom circular button design to be used in game and in turorial
 * 
 * @author Daniel Park
 */

public class LetterCircleButton extends JButton {

    public static final String PICKED_PROPERTY = "picked";

    private static final Color IDLE_FILL = new Color(235, 235, 235);

    private boolean picked;

    /**
     * LetterCircleButton Constructor
     * 
     * @param letter A String with the name
     */
    public LetterCircleButton(String letter) {
        super(letter);
        setPreferredSize(new java.awt.Dimension(86, 86));
        setMinimumSize(new java.awt.Dimension(86, 86));
        setMaximumSize(new java.awt.Dimension(86, 86));
        setFont(new Font("SansSerif", Font.BOLD, 32));
        setFocusPainted(false);
        setFocusable(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setForeground(Color.BLACK);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }


    /**
     * Returns the current selection state of the button.
     * 
     * @return A bool, true if the button is picked
     */
    public boolean isPicked() {
        return picked;
    }

    /** 
     * Updates the picked status of the button
     * 
     * @param picked A bool, true if the button has been picked
     */
    public void setPicked(boolean picked) {
        boolean old = this.picked;
        this.picked = picked;
        firePropertyChange(PICKED_PROPERTY, old, picked);
        repaint();
    }

    /** draws the button */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pad = 3;
        int w = getWidth() - 2 * pad;
        int h = getHeight() - 2 * pad;

        if (picked) {
            g2.setColor(Color.BLACK);
            g2.fillOval(pad, pad, w, h);
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(IDLE_FILL);
            g2.fillOval(pad, pad, w, h);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(pad, pad, w, h);
            g2.setColor(Color.BLACK);
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics(getFont());
        String text = getText();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 2;
        g2.drawString(text, textX, textY);
        g2.dispose();
    }

    /** Overrides the default hit-detection to ensure that only clicks inside the circle count
     * 
     * @param x The x-coordinate of the mouse click
     * @param y The y-coordinate of the mouse click
     * @return A Bool, true if the mouse click was in the circle
     */
    @Override
    public boolean contains(int x, int y) {
        Shape shape = new Ellipse2D.Float(3, 3, getWidth() - 6, getHeight() - 6);
        return shape.contains(x, y);
    }
}
