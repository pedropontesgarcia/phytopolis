package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.syndic8.phytopolis.util.Controllers;
import com.syndic8.phytopolis.util.XBoxController;

/**
 * Class for reading player input.
 * <p>
 * This supports both a keyboard and X-Box controller. In previous solutions, we only
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */

public class InputController {

    // Sensitivity for moving crosshair with gameplay
    private static final float GP_ACCELERATE = 1.0f;
    private static final float GP_MAX_SPEED = 10.0f;
    private static final float GP_THRESHOLD = 0.01f;

    /**
     * The singleton instance of the input controller
     */
    private static InputController theController = null;
    /**
     * The crosshair position (for raddoll)
     */
    private final Vector2 crosshair;

    // Fields to manage buttons
    /**
     * The crosshair cache (for using as a return value)
     */
    private final Vector2 crosscache;
    /**
     * An X-Box controller (if it is connected)
     */
    XBoxController xbox;
    /**
     * Whether the reset button was pressed.
     */
    private boolean resetPressed;
    private boolean resetPrevious;
    /**
     * Whether the button to advanced worlds was pressed.
     */
    private boolean nextPressed;
    private boolean nextPrevious;
    /**
     * Whether the button to step back worlds was pressed.
     */
    private boolean prevPressed;
    private boolean prevPrevious;
    /**
     * Whether the primary action button was pressed.
     */
    private boolean primePressed;
    private boolean primePrevious;
    /**
     * Whether the secondary action button was pressed.
     */
    private boolean secondPressed;
    private boolean secondPrevious;
    /**
     * Whether the teritiary action button was pressed.
     */
    private boolean tertiaryPressed;
    /**
     * Whether the debug toggle was pressed.
     */
    private boolean debugPressed;
    private boolean debugPrevious;
    /**
     * Whether the exit button was pressed.
     */
    private boolean exitPressed;
    private boolean exitPrevious;
    private boolean mousePressed;
    private boolean mousePressedPrevious;
    private boolean growRightPressed;
    private boolean growLeftPressed;
    private boolean growUpPressed;
    private boolean growRightPrevious;
    private boolean growLeftPrevious;
    private boolean growUpPrevious;
    private boolean dropPressed;
    private boolean dropPrevious;
    private boolean extinguishFirePressed;
    private boolean extinguishFirePrevious;
    private float growX;
    private float growY;
    /**
     * How much did we move horizontally?
     */
    private float horizontal;
    /**
     * How much did we move vertically?
     */
    private float vertical;
    /**
     * For the gamepad crosshair control
     */
    private float momentum;

    /**
     * Creates a new input controller
     * <p>
     * The input controller attempts to connect to the X-Box controller at device 0,
     * if it exists.  Otherwise, it falls back to the keyboard control.
     */
    public InputController() {
        // If we have a game-pad for id, then use it.
        Array<XBoxController> controllers = Controllers.get()
                .getXBoxControllers();
        //Array<XBoxController> controllers = new Array<>();
        if (controllers.size > 0) {
            xbox = controllers.get(0);
        } else {
            xbox = null;
        }
        crosshair = new Vector2();
        crosscache = new Vector2();
    }

    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    /**
     * Returns the amount of sideways movement.
     * <p>
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the amount of vertical movement.
     * <p>
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }

    public float getGrowX() {
        return growX;
    }

    public float getGrowY() {
        return growY;
    }

    /**
     * Returns the current position of the crosshairs on the screen.
     * <p>
     * This value does not return the actual reference to the crosshairs position.
     * That way this method can be called multiple times without any fair that
     * the position has been corrupted.  However, it does return the same object
     * each time.  So if you modify the object, the object will be reset in a
     * subsequent call to this getter.
     *
     * @return the current position of the crosshairs on the screen.
     */
    public Vector2 getCrossHair() {
        return crosscache.set(crosshair);
    }

    /**
     * Returns true if the primary action button was pressed.
     * <p>
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the primary action button was pressed.
     */
    public boolean didPrimary() {
        return primePressed && !primePrevious;
    }

    /**
     * Returns true if the secondary action button was pressed.
     * <p>
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didSecondary() {
        return secondPressed && !secondPrevious;
    }

    /**
     * Returns true if the tertiary action button was pressed.
     * <p>
     * This is a sustained button. It will returns true as long as the player
     * holds it down.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didTertiary() {
        return tertiaryPressed;
    }

    public boolean didMousePress() {
        return mousePressed && !mousePressedPrevious;
    }

    public boolean didDrop() {
        return dropPressed & !dropPrevious;
    }

    public boolean didExtinguish() {
        return extinguishFirePressed & !extinguishFirePrevious;
    }

    public boolean didGrowRight() {
        return growRightPressed && !growRightPrevious;
    }

    public boolean didGrowLeft() {
        return growLeftPressed && !growLeftPrevious;
    }

    public boolean didGrowUp() {
        return growUpPressed && !growUpPrevious;
    }

    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed && !resetPrevious;
    }

    /**
     * Returns true if the player wants to go to the next level.
     *
     * @return true if the player wants to go to the next level.
     */
    public boolean didAdvance() {
        return nextPressed && !nextPrevious;
    }

    /**
     * Returns true if the player wants to go to the previous level.
     *
     * @return true if the player wants to go to the previous level.
     */
    public boolean didRetreat() {
        return prevPressed && !prevPrevious;
    }

    /**
     * Returns true if the player wants to go toggle the debug mode.
     *
     * @return true if the player wants to go toggle the debug mode.
     */
    public boolean didDebug() {
        return debugPressed && !debugPrevious;
    }

    /**
     * Returns true if the exit button was pressed.
     *
     * @return true if the exit button was pressed.
     */
    public boolean didExit() {
        return exitPressed && !exitPrevious;
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     * <p>
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    public void readInput(Rectangle bounds, Vector2 scale) {
        // Copy state from last animation frame
        // Helps us ignore buttons that are held down
        primePrevious = primePressed;
        secondPrevious = secondPressed;
        resetPrevious = resetPressed;
        debugPrevious = debugPressed;
        exitPrevious = exitPressed;
        nextPrevious = nextPressed;
        prevPrevious = prevPressed;
        growRightPrevious = growRightPressed;
        growLeftPrevious = growLeftPressed;
        growUpPrevious = growUpPressed;
        mousePressedPrevious = mousePressed;
        dropPrevious = dropPressed;
        extinguishFirePrevious = extinguishFirePressed;

        // Check to see if a GamePad is connected
        if (xbox != null && xbox.isConnected()) {
            readGamepad(bounds, scale);
            readKeyboard(bounds, scale, true); // Read as a back-up
        } else {
            readKeyboard(bounds, scale, false);
        }
    }

    /**
     * Reads input from an X-Box controller connected to this computer.
     * <p>
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    private void readGamepad(Rectangle bounds, Vector2 scale) {
        resetPressed = xbox.getStart();
        exitPressed = xbox.getBack();
        nextPressed = xbox.getRBumper();
        prevPressed = xbox.getLBumper();
        primePressed = xbox.getA();
        debugPressed = xbox.getY();

        // Increase animation frame, but only if trying to move
        horizontal = xbox.getLeftX();
        vertical = xbox.getLeftY();
        secondPressed = xbox.getRightTrigger() > 0.6f;

        // Move the crosshairs with the right stick.
        tertiaryPressed = xbox.getA();
        crosscache.set(xbox.getLeftX(), xbox.getLeftY());
        if (crosscache.len2() > GP_THRESHOLD) {
            momentum += GP_ACCELERATE;
            momentum = Math.min(momentum, GP_MAX_SPEED);
            crosscache.scl(momentum);
            crosscache.scl(1 / scale.x, 1 / scale.y);
            crosshair.add(crosscache);
        } else {
            momentum = 0;
        }
        clampPosition(bounds);
    }

    /**
     * Reads input from the keyboard.
     * <p>
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     * @param secondary true if the keyboard should give priority to a gamepad
     */
    private void readKeyboard(Rectangle bounds,
                              Vector2 scale,
                              boolean secondary) {
        // Give priority to gamepad results
        resetPressed = (secondary && resetPressed) ||
                (Gdx.input.isKeyPressed(Input.Keys.R));
        debugPressed = (secondary && debugPressed) ||
                (Gdx.input.isKeyPressed(Input.Keys.K));
        primePressed = (secondary && primePressed) ||
                (Gdx.input.isKeyPressed(Input.Keys.W));
        secondPressed = (secondary && secondPressed) ||
                (Gdx.input.isKeyPressed(Input.Keys.ENTER));
        prevPressed = (secondary && prevPressed) ||
                (Gdx.input.isKeyPressed(Input.Keys.P));
        nextPressed = (secondary && nextPressed) ||
                (Gdx.input.isKeyPressed(Input.Keys.N));
        exitPressed = (secondary && exitPressed) ||
                (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));

        // Directional controls
        horizontal = (secondary ? horizontal : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontal -= 1.0f;
        }

        vertical = (secondary ? vertical : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            vertical += 1.0f;
        }

        dropPressed = (secondary && dropPressed);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dropPressed = true;
        }

        mousePressed = (secondary && mousePressed);
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            mousePressed = true;
            growX = Gdx.input.getX();
            growY = Gdx.graphics.getHeight() - Gdx.input.getY();
        }
        extinguishFirePressed = (secondary && extinguishFirePressed);
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            extinguishFirePressed = true;
        }
        growRightPressed = (secondary && growRightPressed);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            growRightPressed = true;
        }
        growLeftPressed = (secondary && growLeftPressed);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            growLeftPressed = true;
        }
        growUpPressed = (secondary && growUpPressed);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            growUpPressed = true;
        }
        // Mouse results
        tertiaryPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        crosshair.set(Gdx.input.getX(), Gdx.input.getY());
        crosshair.scl(1 / scale.x, -1 / scale.y);
        crosshair.y += bounds.height;
        clampPosition(bounds);
    }

    /**
     * Clamp the cursor position so that it does not go outside the window
     * <p>
     * While this is not usually a problem with mouse control, this is critical
     * for the gamepad controls.
     */
    private void clampPosition(Rectangle bounds) {
        crosshair.x = Math.max(bounds.x,
                               Math.min(bounds.x + bounds.width, crosshair.x));
        crosshair.y = Math.max(bounds.y,
                               Math.min(bounds.y + bounds.height, crosshair.y));
    }

}
