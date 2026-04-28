
import java.awt.GraphicsDevice;
import javax.swing.JFrame;

/**
 * A utility class to manage fullscreen display modes
 * 
 * @author Daniel Park
 */

public final class Fullscreen {

    private Fullscreen() {
    }

    /**
     * Toggles screen mode
     * 
     * @param frame The {@link javax.swing.JFrame} window to be toggled
     */
    public static void toggle(JFrame frame) {
        GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
        if (device.getFullScreenWindow() == frame) {
            device.setFullScreenWindow(null);
        } else {
            device.setFullScreenWindow(frame);
        }
    }
}
