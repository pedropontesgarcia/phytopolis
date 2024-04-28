package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntSet;

/**
 * Class for reading player input.
 */
public class InputController implements InputProcessor {

    private static int height;
    /**
     * The singleton instance of the input controller.
     */
    private static InputController theController;
    private final InputMultiplexer multiplexer;
    private final IntSet keys;
    private boolean updateScheduled;
    private Binding bindingToUpdate;
    private int growBranchModKey;
    private int growBranchButton;
    private int growLeafModKey;
    private int growLeafButton;
    private int jumpKey;
    private int leftKey;
    private int dropKey;
    private int rightKey;
    private int exitKey;
    /**
     * Whether the grow branch mod key was pressed.
     */
    private boolean growBranchModDown;
    /**
     * Whether the grow branch button was pressed.
     */
    private boolean growBranchButtonPressed;
    private boolean growBranchButtonPrevious;
    /**
     * Whether the grow leaf mod key was pressed.
     */
    private boolean growLeafModDown;
    /**
     * Whether the grow leaf button was pressed.
     */
    private boolean growLeafButtonPressed;
    private boolean growLeafButtonPrevious;
    /**
     * Whether the jump key was pressed.
     */
    private boolean jumpKeyPressed;
    private boolean jumpKeyPrevious;
    /**
     * Whether the left key was pressed.
     */
    private boolean leftKeyPressed;
    /**
     * Whether the drop key is down.
     */
    private boolean dropKeyDown;
    /**
     * Whether the right key was pressed.
     */
    private boolean rightKeyPressed;
    /**
     * Whether the exit key was pressed.
     */
    private boolean exitKeyPressed;
    private boolean exitKeyPrevious;
    /**
     * Whether the mouse left click was pressed.
     */
    private boolean mousePressed;
    private boolean mousePrevious;
    private float mouseX;
    private float mouseY;
    private boolean scrollReset;
    private float horizontal;
    private float scrolled;

    /**
     * Creates a new input controller.
     */
    public InputController() {
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        keys = new IntSet();

        // Setting defaults
        setDefaults();
    }

    private void setDefaults() {
        // Default: NONE
        growBranchModKey = -1;
        // Default: MOUSE LEFT
        growBranchButton = Input.Buttons.LEFT;
        // Default: SHIFT
        growLeafModKey = Input.Keys.SHIFT_LEFT;
        // Default: MOUSE LEFT
        growLeafButton = Input.Buttons.LEFT;
        // Default: W
        jumpKey = Input.Keys.W;
        // Default: A
        leftKey = Input.Keys.A;
        // Default: S
        dropKey = Input.Keys.S;
        // Default: D
        rightKey = Input.Keys.D;
        // Default: ESC
        exitKey = Input.Keys.ESCAPE;
    }

    /**
     * Returns the singleton instance of the input controller.
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    public boolean didGrowBranch() {
        boolean didGrowBranchKey =
                growBranchButtonPressed && !growBranchButtonPrevious;
        return didGrowBranchKey && isGrowBranchModDown();
    }

    public boolean isGrowBranchModDown() {
        if (growBranchModKey == -1) return true;
        return growBranchModDown;
    }

    public boolean isGrowBranchModSet() {
        return growBranchModKey != -1;
    }

    public boolean didGrowLeaf() {
        boolean didGrowLeafKey =
                growLeafButtonPressed && !growLeafButtonPrevious;
        return didGrowLeafKey && isGrowLeafModDown();
    }

    public boolean isGrowLeafModDown() {
        if (growLeafModKey == -1) return true;
        return growLeafModDown;
    }

    public boolean isGrowLeafModSet() {
        return growLeafModKey != -1;
    }

    public boolean didJump() {
        return jumpKeyPressed && !jumpKeyPrevious;
    }

    public boolean didExit() {
        return exitKeyPressed && !exitKeyPrevious;
    }

    public InputMultiplexer getMultiplexer() {
        return multiplexer;
    }

    public void setHeight(int h) {
        height = h;
    }

    /**
     * Returns the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    public float getMouseX() {
        return mouseX;
    }

    public float getMouseY() {
        return mouseY;
    }

    public boolean didMousePress() {
        return mousePressed && !mousePrevious;
    }

    public boolean isDropKeyDown() {
        return dropKeyDown;
    }

    public boolean didScrollReset() {
        return scrollReset;
    }

    public float getScrolled() {
        return scrolled;
    }

    public void resetScrolled() {
        scrolled = 0f;
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     */
    public void readInput() {
        // Copy previous state
        growBranchButtonPrevious = growBranchButtonPressed;
        growLeafButtonPrevious = growLeafButtonPressed;
        jumpKeyPrevious = jumpKeyPressed;
        exitKeyPrevious = exitKeyPressed;
        mousePrevious = mousePressed;

        readKeyboard();
    }

    /**
     * Reads input from the keyboard.
     */
    private void readKeyboard() {
        // Read mouse position
        mouseX = Gdx.input.getX();
        mouseY = Gdx.input.getY();

        // Read mouse and key input
        if (growBranchModKey != -1)
            growBranchModDown = Gdx.input.isKeyPressed(growBranchModKey);
        growBranchButtonPressed = Gdx.input.isButtonPressed(growBranchButton);
        if (growLeafModKey != -1)
            growLeafModDown = Gdx.input.isKeyPressed(growLeafModKey);
        growLeafButtonPressed = Gdx.input.isButtonPressed(growLeafButton);
        jumpKeyPressed = Gdx.input.isKeyPressed(jumpKey);
        leftKeyPressed = Gdx.input.isKeyPressed(leftKey);
        dropKeyDown = Gdx.input.isKeyPressed(dropKey);
        rightKeyPressed = Gdx.input.isKeyPressed(rightKey);
        exitKeyPressed = Gdx.input.isKeyPressed(exitKey);
        mousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        // Manage horizontal input
        horizontal = 0;
        if (rightKeyPressed) {
            horizontal += 1.0f;
        }
        if (leftKeyPressed) {
            horizontal -= 1.0f;
        }

        // Manage scroll
        if (Gdx.input.isKeyPressed(Input.Keys.X)) {
            scrollReset = true;
        }
    }

    public void updateBinding(Binding binding) {
        updateScheduled = true;
        bindingToUpdate = binding;
    }

    public boolean isUpdateScheduled() {
        return updateScheduled;
    }

    @Override
    public boolean keyDown(int i) {
        keys.add(i);
        if (updateScheduled) {
            updateScheduled = false;
            switch (bindingToUpdate) {
                case GROW_BRANCH_MOD_KEY:
                    if (i == Input.Keys.ESCAPE) growBranchModKey = -1;
                    else growBranchModKey = i;
                    return true;
                case GROW_LEAF_MOD_KEY:
                    if (i == Input.Keys.ESCAPE) growLeafModKey = -1;
                    else growLeafModKey = i;
                    return true;
                case JUMP_KEY:
                    if (i != Input.Keys.ESCAPE) jumpKey = i;
                    return true;
                case LEFT_KEY:
                    if (i != Input.Keys.ESCAPE) leftKey = i;
                    return true;
                case DROP_KEY:
                    if (i != Input.Keys.ESCAPE) dropKey = i;
                    return true;
                case RIGHT_KEY:
                    if (i != Input.Keys.ESCAPE) rightKey = i;
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        keys.remove(i);
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        if (updateScheduled) {
            updateScheduled = false;
            switch (bindingToUpdate) {
                case GROW_BRANCH_BUTTON:
                    growBranchButton = i3;
                    return true;
                case GROW_LEAF_BUTTON:
                    growLeafButton = i3;
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        scrolled -= v1;
        scrolled = Math.max(0, Math.min(height, scrolled));
        return false;
    }

    public String getBindingString(Binding b) {
        switch (b) {
            case GROW_BRANCH_MOD_KEY:
                if (growBranchModKey != -1)
                    return Input.Keys.toString(growBranchModKey);
                break;
            case GROW_BRANCH_BUTTON:
                return getMouseButtonString(growBranchButton);
            case GROW_LEAF_MOD_KEY:
                if (growLeafModKey != -1)
                    return Input.Keys.toString(growLeafModKey);
                break;
            case GROW_LEAF_BUTTON:
                return getMouseButtonString(growLeafButton);
            case JUMP_KEY:
                return Input.Keys.toString(jumpKey);
            case LEFT_KEY:
                return Input.Keys.toString(leftKey);
            case DROP_KEY:
                return Input.Keys.toString(dropKey);
            case RIGHT_KEY:
                return Input.Keys.toString(rightKey);
        }
        return "None";
    }

    private String getMouseButtonString(int button) {
        if (button == Input.Buttons.LEFT) return "Mouse Left";
        if (button == Input.Buttons.RIGHT) return "Mouse Right";
        if (button == Input.Buttons.MIDDLE) return "Mouse Middle";
        if (button == Input.Buttons.BACK) return "Mouse Back";
        return "Unknown";
    }

    public enum Binding {
        GROW_BRANCH_MOD_KEY,
        GROW_BRANCH_BUTTON,
        GROW_LEAF_MOD_KEY,
        GROW_LEAF_BUTTON,
        JUMP_KEY,
        LEFT_KEY,
        DROP_KEY,
        RIGHT_KEY
    }

}
