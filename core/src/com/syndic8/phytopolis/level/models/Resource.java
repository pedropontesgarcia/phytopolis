package com.syndic8.phytopolis.level.models;

public abstract class Resource extends CircleObject {

    /**
     * Radius for resource collisions
     */
    private static final int RESOURCE_RADIUS = 1;
    private static final int REGEN_DELAY = 15;
    private static final int MAX_REGEN = 100;
    private int currRegen;
    private int currDelay;

    /**
     * Creates a resource object.
     */
    public Resource(float x, float y) {
        super(x, y, RESOURCE_RADIUS);
        currRegen = MAX_REGEN;
        currDelay = 0;
    }

    public boolean isFull() {
        return currRegen == MAX_REGEN;
    }

    public float getRegenRatio() {
        return (float) currRegen / MAX_REGEN;
    }

    public void clear() {
        System.out.println("clear");
        currRegen = 0;
        currDelay = 0;
        body = null;
    }

    public void regenerate() {
        if (currRegen < MAX_REGEN) {
            System.out.println(currRegen);
            currDelay++;
            //System.out.println(framesOnGround);
            //System.out.println(currWater);
            if (currDelay >= REGEN_DELAY) {
                //System.out.println("IN IF");
                currDelay -= REGEN_DELAY;
                //System.out.println("NOT MAX");
                currRegen++;

            }
        }
    }

}
