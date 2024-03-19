package com.syndic8.phytopolis.level;

public class ResourceController {
    /** Maximum amount of water that can be stored */
    private final int MAX_WATER = 100;
    /** Amount of water required to grow a branch */
    private final int BRANCH_AMT = 5;
    /** Amount of water required to grow a leaf */
    private final int LEAF_AMT = 5;
    /** Amount of water required to extinguish a fire */
    private final int FIRE_AMT = 20;
    /** Current amount of water stored */
    private int water;

    public ResourceController() {
        water = MAX_WATER;
    }

}
