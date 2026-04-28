
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Description:
 * 
 * 1. State Management (isPicked / setPicked)
 * - Verifies the initialization of the 'picked' state.
 * - Confirms that setPicked correctly updates the internal boolean state.
 * 
 * 2. Event Dispatching
 * - Ensures that changing the 'picked' status fires a PropertyChangeEvent with the expected "picked" property name.
 * - Validates that the event correctly provides both the old and new values.
 * 
 * 3. Geometry & Hit Detection (contains)
 * - Tests that the button's custom hit detection correctly identifies points inside vs outside the circular boundary.
 * - Verifies that the center point is active, while the corners (outside the circle but inside the rectangle) are inactive.
 */
class LetterCircleButtonTest {

    @Test
    void isPicked() {
        LetterCircleButton button = new LetterCircleButton("A");
        assertFalse(button.isPicked(), "Button should not be picked by default");
        
        button.setPicked(true);
        assertTrue(button.isPicked(), "Button status should be true after setPicked(true)");
    }

    @Test
    void setPicked() {
        LetterCircleButton button = new LetterCircleButton("Z");
        final int[] changeCount = {0};
        
        button.addPropertyChangeListener(LetterCircleButton.PICKED_PROPERTY, evt -> {
            changeCount[0]++;
            assertEquals(false, evt.getOldValue());
            assertEquals(true, evt.getNewValue());
        });

        button.setPicked(true);
        assertEquals(1, changeCount[0], "PropertyChangeListener should have been notified exactly once");
        assertTrue(button.isPicked());
    }

    @Test
    void contains() {
        LetterCircleButton button = new LetterCircleButton("M");
        // Set fixed size for deterministic testing of the circle math
        button.setSize(100, 100);

        // Center (50, 50) - should be inside
        assertTrue(button.contains(50, 50), "Center point should be inside the button");

        // Corner (0, 0) - should be outside the circle
        assertFalse(button.contains(0, 0), "Corner (0,0) should be outside the circular boundary");

        // Corner (99, 99) - should be outside the circle
        assertFalse(button.contains(99, 99), "Corner (99,99) should be outside the circular boundary");

        // Edge case: Point on the circular line (approx)
        // With size 100x100 and 3px padding, the ellipse bounds are (3, 3, 94, 94).
        // Center is 50,50. Point at (50, 4) should be just inside the top edge.
        assertTrue(button.contains(50, 4), "Top edge point should be inside");
        assertFalse(button.contains(50, 2), "Point just above the circle should be outside");
    }
}