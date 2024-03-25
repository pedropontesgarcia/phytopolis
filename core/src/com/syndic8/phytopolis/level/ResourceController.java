package com.syndic8.phytopolis.level;

import com.syndic8.phytopolis.level.models.Player;

public class ResourceController {
    /** Maximum amount of water that can be stored */
    private final int MAX_WATER = 100;
//    /** Amount of water required to grow a branch */
//    private final int BRANCH_AMT = 5;
    /** Amount of water required to grow a branch/leaf */
    private final int GROW_AMT = 5;
    /** Amount of water required to extinguish a fire */
    private final int FIRE_AMT = 20;

    private int currSun;
    /** Current amount of water stored */
    private int currWater;
    /** Frames on ground (for getting water) */
    private int framesOnGround;

    public ResourceController() {
        currWater = MAX_WATER;
        framesOnGround = 0;
    }

    public int getCurrWater() {
        return currWater;
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
    }



}
