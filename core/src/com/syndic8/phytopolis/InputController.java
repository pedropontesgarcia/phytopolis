package com.syndic8.phytopolis;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.syndic8.phytopolis.util.OSUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for reading player input.
 */
public class InputController implements InputProcessor {

    private static float height;
    /**
     * The singleton instance of the input controller.
     */
    private static InputController theController;
    private final InputMultiplexer multiplexer;
    private final IntSet keys;
    private final IntSet assignedKeys;
    private final Preferences preferences;
    private final Map<String, Integer> defaultBindings;
    private final JsonValue settingsJson;
    private final FileHandle configFile;
    private Map<String, Integer> bindings;
    private boolean updateScheduled;
    private Binding bindingToUpdate;
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
    /**
     * Mouse screen coordinates.
     */
    private float mouseX;
    private float mouseY;
    /**
     * Scroll input.
     */
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
        assignedKeys = new IntSet();
        preferences = Gdx.app.getPreferences("phytopolis_settings");
        defaultBindings = new HashMap<>();
        bindings = new HashMap<>();
        configFile = Gdx.files.absolute(OSUtils.getConfigFile());
        JsonReader settingsJsonReader = new JsonReader();
        settingsJson = settingsJsonReader.parse(configFile);

        // Reading preferences and otherwise setting defaults
        setBindings();
    }

    private void setBindings() {
        exitKey = Input.Keys.ESCAPE;
        defaultBindings.put("growBranchModKey", -1);
        defaultBindings.put("growBranchButton", Input.Buttons.LEFT);
        defaultBindings.put("growLeafModKey", Input.Keys.SHIFT_LEFT);
        defaultBindings.put("growLeafButton", Input.Buttons.LEFT);
        defaultBindings.put("jumpKey", Input.Keys.W);
        defaultBindings.put("leftKey", Input.Keys.A);
        defaultBindings.put("dropKey", Input.Keys.S);
        defaultBindings.put("rightKey", Input.Keys.D);
        for (String key : defaultBindings.keySet()) {
            int actualVal = settingsJson.getInt(key);
            bindings.put(key, actualVal);
            if (actualVal != -1) assignedKeys.add(actualVal);
        }
        updateAssigned();
    }

    private void updateAssigned() {
        assignedKeys.clear();
        if (bindings.get("growBranchModKey") != -1)
            assignedKeys.add(bindings.get("growBranchModKey"));
        if (bindings.get("growLeafModKey") != -1)
            assignedKeys.add(bindings.get("growLeafModKey"));
        assignedKeys.add(bindings.get("jumpKey"));
        assignedKeys.add(bindings.get("leftKey"));
        assignedKeys.add(bindings.get("dropKey"));
        assignedKeys.add(bindings.get("rightKey"));
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

    public void resetBindings() {
        bindings = new HashMap<>(defaultBindings);
        updateAssigned();
        updatePreferences();
    }

    private void updatePreferences() {
        for (String key : bindings.keySet()) {
            settingsJson.get(key).set(bindings.get(key), null);
        }
        configFile.writeString(settingsJson.prettyPrint(JsonWriter.OutputType.json,
                                                        0), false);
    }

    public boolean didGrowBranch() {
        boolean didGrowBranchKey =
                growBranchButtonPressed && !growBranchButtonPrevious;
        return didGrowBranchKey && isGrowBranchModDown();
    }

    public boolean isGrowBranchModDown() {
        if (bindings.get("growBranchModKey") == -1) return true;
        return growBranchModDown;
    }

    public boolean isGrowBranchModSet() {
        return bindings.get("growBranchModKey") != -1;
    }

    public boolean didGrowLeaf() {
        boolean didGrowLeafKey =
                growLeafButtonPressed && !growLeafButtonPrevious;
        return didGrowLeafKey && isGrowLeafModDown();
    }

    public boolean isGrowLeafModDown() {
        if (bindings.get("growLeafModKey") == -1) return true;
        return growLeafModDown;
    }

    public boolean isGrowLeafModSet() {
        return bindings.get("growLeafModKey") != -1;
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

    public void setHeight(float h) {
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
        if (bindings.get("growBranchModKey") != -1)
            growBranchModDown = Gdx.input.isKeyPressed(bindings.get(
                    "growBranchModKey"));
        growBranchButtonPressed = Gdx.input.isButtonPressed(bindings.get(
                "growBranchButton"));
        if (bindings.get("growLeafModKey") != -1)
            growLeafModDown = Gdx.input.isKeyPressed(bindings.get(
                    "growLeafModKey"));
        growLeafButtonPressed = Gdx.input.isButtonPressed(bindings.get(
                "growLeafButton"));
        jumpKeyPressed = Gdx.input.isKeyPressed(bindings.get("jumpKey"));
        leftKeyPressed = Gdx.input.isKeyPressed(bindings.get("leftKey"));
        dropKeyDown = Gdx.input.isKeyPressed(bindings.get("dropKey"));
        rightKeyPressed = Gdx.input.isKeyPressed(bindings.get("rightKey"));
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

//    public boolean didWin() {
//        return Gdx.input.isKeyPressed(Input.Keys.L);
//    }

    @Override
    public boolean keyDown(int i) {
        keys.add(i);
        if (updateScheduled) {
            updateScheduled = false;
            int key = (i == Input.Keys.ESCAPE ? -1 : i);
            boolean conflictBetweenBranchAndLeaf;
            boolean conflictWithOtherBindings = assignedKeys.contains(key);
            switch (bindingToUpdate) {
                case GROW_BRANCH_MOD_KEY:
                    conflictBetweenBranchAndLeaf = (
                            bindings.get("growBranchButton") ==
                                    bindings.get("growLeafButton") &&
                                    key == bindings.get("growLeafModKey"));
                    if (conflictWithOtherBindings &&
                            !(key == bindings.get("growLeafModKey")))
                        return true;
                    assignedKeys.remove(bindings.get("growBranchModKey"));
                    bindings.put("growBranchModKey", key);
                    if (conflictBetweenBranchAndLeaf) {
                        bindings.put("growLeafButton",
                                     bindings.get("growLeafButton") ==
                                             Input.Buttons.LEFT ?
                                             Input.Buttons.RIGHT :
                                             Input.Buttons.LEFT);
                    }
                    if (key != -1) assignedKeys.add(key);
                    updatePreferences();
                    return true;
                case GROW_LEAF_MOD_KEY:
                    conflictBetweenBranchAndLeaf = (
                            bindings.get("growBranchButton") ==
                                    bindings.get("growLeafButton") &&
                                    key == bindings.get("growBranchModKey"));
                    if (conflictWithOtherBindings &&
                            !(key == bindings.get("growBranchModKey")))
                        return true;
                    assignedKeys.remove(bindings.get("growLeafModKey"));
                    bindings.put("growLeafModKey", key);
                    if (conflictBetweenBranchAndLeaf) {
                        bindings.put("growBranchButton",
                                     bindings.get("growBranchButton") ==
                                             Input.Buttons.LEFT ?
                                             Input.Buttons.RIGHT :
                                             Input.Buttons.LEFT);
                    }
                    if (key != -1) assignedKeys.add(key);
                    updatePreferences();
                    return true;
                case JUMP_KEY:
                    if (i != Input.Keys.ESCAPE && !assignedKeys.contains(i)) {
                        assignedKeys.remove(bindings.get("jumpKey"));
                        bindings.put("jumpKey", i);
                        assignedKeys.add(bindings.get("jumpKey"));
                    }
                    updatePreferences();
                    return true;
                case LEFT_KEY:
                    if (i != Input.Keys.ESCAPE && !assignedKeys.contains(i)) {
                        assignedKeys.remove(bindings.get("leftKey"));
                        bindings.put("leftKey", i);
                        assignedKeys.add(bindings.get("leftKey"));
                    }
                    updatePreferences();
                    return true;
                case DROP_KEY:
                    if (i != Input.Keys.ESCAPE && !assignedKeys.contains(i)) {
                        assignedKeys.remove(bindings.get("dropKey"));
                        bindings.put("dropKey", i);
                        assignedKeys.add(bindings.get("dropKey"));
                    }
                    updatePreferences();
                    return true;
                case RIGHT_KEY:
                    if (i != Input.Keys.ESCAPE && !assignedKeys.contains(i)) {
                        assignedKeys.remove(bindings.get("rightKey"));
                        bindings.put("rightKey", i);
                        assignedKeys.add(bindings.get("rightKey"));
                    }
                    updatePreferences();
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
            if (i3 != Input.Buttons.LEFT && i3 != Input.Buttons.RIGHT)
                return true;
            switch (bindingToUpdate) {
                case GROW_BRANCH_BUTTON:
                    bindings.put("growBranchButton", i3);
                    if (bindings.get("growBranchButton")
                            .equals(bindings.get("growLeafButton")) &&
                            bindings.get("growBranchModKey")
                                    .equals(bindings.get("growLeafModKey"))) {
                        bindings.put("growLeafButton",
                                     bindings.get("growLeafButton") ==
                                             Input.Buttons.LEFT ?
                                             Input.Buttons.RIGHT :
                                             Input.Buttons.LEFT);
                    }
                    updatePreferences();
                    return true;
                case GROW_LEAF_BUTTON:
                    bindings.put("growLeafButton", i3);
                    if (bindings.get("growBranchButton")
                            .equals(bindings.get("growLeafButton")) &&
                            bindings.get("growBranchModKey")
                                    .equals(bindings.get("growLeafModKey"))) {
                        bindings.put("growBranchButton",
                                     bindings.get("growBranchButton") ==
                                             Input.Buttons.LEFT ?
                                             Input.Buttons.RIGHT :
                                             Input.Buttons.LEFT);
                    }
                    updatePreferences();
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
        scrolled = Math.max(-height, Math.min(height, scrolled));
        return false;
    }

    public String getBindingString(Binding b) {
        switch (b) {
            case GROW_BRANCH_MOD_KEY:
                if (!bindings.get("growBranchModKey").equals(-1))
                    return Input.Keys.toString(bindings.get("growBranchModKey"));
                return "None";
            case GROW_BRANCH_BUTTON:
                return getMouseButtonString(bindings.get("growBranchButton"));
            case GROW_LEAF_MOD_KEY:
                if (!bindings.get("growLeafModKey").equals(-1))
                    return Input.Keys.toString(bindings.get("growLeafModKey"));
                return "None";
            case GROW_LEAF_BUTTON:
                return getMouseButtonString(bindings.get("growLeafButton"));
            case JUMP_KEY:
                if (!bindings.get("jumpKey").equals(-1))
                    return Input.Keys.toString(bindings.get("jumpKey"));
                break;
            case LEFT_KEY:
                if (!bindings.get("leftKey").equals(-1))
                    return Input.Keys.toString(bindings.get("leftKey"));
                break;
            case DROP_KEY:
                if (!bindings.get("dropKey").equals(-1))
                    return Input.Keys.toString(bindings.get("dropKey"));
                break;
            case RIGHT_KEY:
                if (!bindings.get("rightKey").equals(-1))
                    return Input.Keys.toString(bindings.get("rightKey"));
                break;
        }
        return "Unassigned";
    }

    private String getMouseButtonString(int button) {
        if (button == Input.Buttons.LEFT) return "Mouse Left";
        if (button == Input.Buttons.RIGHT) return "Mouse Right";
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
