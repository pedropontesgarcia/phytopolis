package com.syndic8.phytopolis.level;

import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.Tilemap;

public class ResourceController {

    /**
     * Maximum amount of water that can be stored
     */
    private final int MAX_WATER = 100;

    private final int MAX_SUN = 8;

    private final int STARTING_SUN = 1;
    /**
     * Amount of water required to grow a leaf
     */
    private final int LEAF_GROW_AMT = 10;
    /**
     * Amount of water required to grow a branch
     */
    private final int BRANCH_GROW_AMT = 5;
    /**
     * Amount of water required to extinguish a fire
     */
    private final int FIRE_AMT = 20;
    /**
     * Amount of sun required for an upgrade
     */
    private final int UPGRADE_AMT = 1;
    private final int SUN_ON_PICKUP = 1;
    private final int WATER_ON_PICKUP = 50;

    /**
     * Controller for UI.
     */
    private final UIController ui;
    /**
     * Current amount of sun stored
     */
    private int currSun;
    /**
     * Current amount of water stored
     */
    private int currWater;

    public ResourceController(GameCanvas c, Tilemap tm) {
        currWater = MAX_WATER;
        currSun = STARTING_SUN;
        ui = new UIController(c, tm);
    }

    public void gatherAssets(AssetDirectory directory) {
        ui.gatherAssets(directory);
    }

    public int getCurrWater() {
        return currWater;
    }

    public void pickupWater() {
        currWater = Math.min(MAX_WATER, currWater + WATER_ON_PICKUP);
    }

    public int getCurrSun() {
        return currSun;
    }

    public void pickupSun() {
        currSun = Math.min(MAX_SUN, currSun + SUN_ON_PICKUP);
        addTime(10);
    }

    public void addTime(float t) {
        ui.addTime(t);
    }

    public boolean fullWater() {
        return currWater == MAX_WATER;
    }

    public boolean fullSun() {
        return currSun == MAX_SUN;
    }

    public void decrementGrowLeaf() {
        if (canGrowLeaf()) {
            currWater -= LEAF_GROW_AMT;
        }
    }

    public boolean canGrowLeaf() {
        return currWater >= LEAF_GROW_AMT;
    }

    public void decrementGrowBranch() {
        if (canGrowBranch()) {
            currWater -= BRANCH_GROW_AMT;
        }
    }

    public boolean canGrowBranch() {
        return currWater >= BRANCH_GROW_AMT;
    }

    public void decrementExtinguish() {
        if (canExtinguish()) {
            currWater -= FIRE_AMT;
        }
    }

    public boolean canExtinguish() {
        return currWater >= FIRE_AMT;
    }

    public void decrementUpgrade() {
        if (!canUpgrade()) {

        } else {
            currSun -= UPGRADE_AMT;
        }
    }

    public boolean canUpgrade() {
        return currSun >= UPGRADE_AMT;
    }

    public void reset() {
        currWater = MAX_WATER;
        currSun = STARTING_SUN;
    }

    public void update(float dt) {
        ui.update(dt, (float) currWater / MAX_WATER, (float) currSun / MAX_SUN);

    }

    public void drawUI(GameCanvas c) {
        ui.draw(c);
    }

    public UIController getUIController() {
        return ui;
    }

}
