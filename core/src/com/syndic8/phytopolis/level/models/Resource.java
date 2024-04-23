package com.syndic8.phytopolis.level.models;

import com.syndic8.phytopolis.util.Tilemap;

public abstract class Resource extends CircleObject {

    /**
     * Radius for resource collisions
     */
    private static final int RESOURCE_RADIUS = 1;
    private static final int REGEN_DELAY = 15;
    private static final int MAX_REGEN = 100;
    public final float width;
    public final float height;
    private int currRegen;
    private int currDelay;

    /**
     * Creates a resource object.
     */
    public Resource(float x,
                    float y,
                    float w,
                    float h,
                    Tilemap tm,
                    float texScl) {
        super(x, y, RESOURCE_RADIUS, tm, texScl);
        currRegen = MAX_REGEN;
        currDelay = 0;
        width = w;
        height = h;
        zIndex = 3;
    }

    public boolean isFull() {
        return currRegen == MAX_REGEN;
    }

    public float getRegenRatio() {
        return (float) currRegen / MAX_REGEN;
    }

    public void clear() {
        currRegen = 0;
        currDelay = 0;
    }

    public void regenerate() {
        if (currRegen < MAX_REGEN) {
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
