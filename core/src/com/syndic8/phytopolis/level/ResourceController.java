package com.syndic8.phytopolis.level;

import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Player;
import com.syndic8.phytopolis.level.models.Resource;
import com.syndic8.phytopolis.util.PooledList;

public class ResourceController {

    /**
     * Maximum amount of water that can be stored
     */
    private final int MAX_WATER = 100;

    private final int MAX_SUN = 8;
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
    private final int WATER_ON_PICKUP = 10;

    /**
     * Controller for UI.
     */
    private final UIController ui;
    /**
     * All pickup resources in world
     */
    protected PooledList<Resource> resources = new PooledList<Resource>();
    private int currSun;
    /**
     * Current amount of water stored
     */
    private int currWater;
    /**
     * Frames on ground (for getting water)
     */
    private int framesOnGround;

    public ResourceController() {
        currWater = MAX_WATER;
        framesOnGround = 0;
        currSun = 0;
        ui = new UIController();
    }

    public void gatherAssets(AssetDirectory directory) {
        ui.gatherAssets(directory);
    }

    public int getCurrWater() {
        return currWater;
    }

    public void pickupWater(float ratio) {
        currWater = Math.min(MAX_WATER,
                             currWater + (int) (WATER_ON_PICKUP * ratio));
    }

    public int getCurrSun() {
        return currSun;
    }

    public void pickupSun() {
        currSun = Math.min(MAX_SUN, currSun + SUN_ON_PICKUP);
    }

    public boolean fullWater() {
        return currWater == MAX_WATER;
    }

    public boolean fullSun() {
        return currSun == MAX_SUN;
    }

    public boolean canGrowLeaf() {
        return currWater >= LEAF_GROW_AMT;
    }

    public boolean canGrowBranch() {
        return currWater >= BRANCH_GROW_AMT;
    }

    public boolean canExtinguish() {
        return currWater >= FIRE_AMT;
    }

    public boolean canUpgrade() { return currSun >= UPGRADE_AMT; }

    public void decrementGrowLeaf() {
        if (!canGrowLeaf()) {
            //System.out.println("NOT ENOUGH WATER!");
        } else {
            currWater -= LEAF_GROW_AMT;
        }
    }

    public void decrementGrowBranch() {
        if (!canGrowBranch()) {
            //System.out.println("NOT ENOUGH WATER!");
        } else {
            currWater -= BRANCH_GROW_AMT;
        }
    }

    public void decrementExtinguish() {
        if (!canExtinguish()) {
            //System.out.println("NOT ENOUGH WATER!");
        } else {
            currWater -= FIRE_AMT;
        }
    }

    public void decrementUpgrade() {
        if (!canUpgrade()) {

        } else {
            currSun -= UPGRADE_AMT;
        }
    }

    //    public void addWater(float x, float y) {
    //        Water w = new Water(x, y);
    //        resources.add(w);
    //    }

    //    public void addSun(float x, float y) {
    //        Sun s = new Sun(x, y);
    //        resources.add(s);
    //    }

    public PooledList<Resource> getResources() {
        return resources;
    }

    public void update() {


    }

    public void draw(GameCanvas c) {
        ui.update((float) currWater / MAX_WATER, (float) currSun / MAX_SUN);
        ui.draw(c);
    }

}
