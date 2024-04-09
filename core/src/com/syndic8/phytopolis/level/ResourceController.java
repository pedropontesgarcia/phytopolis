package com.syndic8.phytopolis.level;

import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Player;

public class ResourceController {

    /**
     * Maximum amount of water that can be stored
     */
    private final int MAX_WATER = 100;

    private final int MAX_SUN = 8;
    //    /** Amount of water required to grow a branch */
    //    private final int BRANCH_AMT = 5;
    /**
     * Amount of water required to grow a branch/leaf
     */
    private final int GROW_AMT = 5;
    /**
     * Amount of water required to extinguish a fire
     */
    private final int FIRE_AMT = 20;
    /**
     * Controller for UI.
     */
    private final UIController ui;
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

    public void setCurrWater(int water) {
        if (!fullWater()) {
            currWater += water;
        }
    }

    public int getCurrSun() {
        return currSun;
    }

    public void setCurrSun(int sun) {
        if (!fullSun())
        {currSun += sun;}
    }

    public boolean fullWater() {
        return currWater == MAX_WATER;
    }

    public boolean fullSun(){
        return currSun == MAX_SUN;
    }

    public boolean canGrow() {
        return currWater >= GROW_AMT;
    }

    public boolean canExtinguish() {
        return currWater >= FIRE_AMT;
    }

    public void decrementGrow() {
        if (!canGrow()) {
            //System.out.println("NOT ENOUGH WATER!");
        } else {
            currWater -= GROW_AMT;
        }
    }

    public void decrementExtinguish() {
        if (!canExtinguish()) {
            //System.out.println("NOT ENOUGH WATER!");
        } else {
            currWater -= FIRE_AMT;
        }
    }

    public void update(Player player) {
        if (player.atBottom()) {
            //System.out.println("AT BOTTOM");
            framesOnGround++;
        }
        //System.out.println(framesOnGround);
        //System.out.println(currWater);
        if (framesOnGround >= 4) {
            //System.out.println("IN IF");
            framesOnGround -= 4;
            if (currWater < MAX_WATER) {
                //System.out.println("NOT MAX");
                currWater++;
            }
        }
        ui.update((float) currWater / MAX_WATER, (float) currSun / MAX_SUN);
    }

    public void draw(GameCanvas c) {
        ui.draw(c);
    }

}
