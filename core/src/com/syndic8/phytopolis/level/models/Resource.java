package com.syndic8.phytopolis.level.models;

import com.syndic8.phytopolis.util.Tilemap;

public abstract class Resource extends CircleObject {

    /**
     * Radius for resource collisions
     */
    private static final int RESOURCE_RADIUS = 1;
    public final float width;
    public final float height;


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
        width = w;
        height = h;
        zIndex = 3;
    }

    public abstract void clear();

}
