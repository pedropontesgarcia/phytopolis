package com.syndic8.phytopolis.level.models;

import com.syndic8.phytopolis.util.Tilemap;

public abstract class Resource extends CircleObject {

    /**
     * Radius for resource collisions
     */
    private static final float RESOURCE_RADIUS = 0.5f;
    public final float width;
    public final float height;

    /**
     * Creates a resource object.
     */
    public Resource(float x,
                    float y,
                    float w,
                    float h,
                    Tilemap.TilemapParams tmp,
                    float texScl) {
        super(x, y, RESOURCE_RADIUS, tmp, texScl);
        width = w;
        height = h;
        zIndex = 3;
    }

    public abstract void clear();

}
