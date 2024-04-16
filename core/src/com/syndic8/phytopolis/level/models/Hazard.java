package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public abstract class Hazard extends CircleObject {

    /**
     * Radius for hazard collisions
     */
    private static final int HAZARD_RADIUS = 30;

    /**
     * The current location of the hazard.
     */
    protected Vector2 location;

    /**
     * Creates a hazard object.
     */
    public Hazard(Tilemap tm, float texScl, Vector2 location) {
        super(HAZARD_RADIUS, tm, texScl);
        this.location = location;
        zIndex = 4;
    }

    /**
     * Gets the current location of the hazard.
     *
     * @return The current location of the hazard.
     */
    public Vector2 getLocation() {
        return location;
    }

    /**
     * Sets a new location for the hazard.
     *
     * @param location The new location of the hazard.
     */
    public void setLocation(Vector2 location) {
        this.location = location;
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    @Override
    public abstract ModelType getType();

    /**
     * Draws the hazard on the provided GameCanvas.
     *
     * @param canvas The GameCanvas on which to draw.
     * @param x      The x position at which to draw.
     * @param y      The y position at which to draw.
     */
    public void draw(GameCanvas canvas, float x, float y) {
        float width = tilemap.getTileWidth() * textureSclInTiles;
        float height = tilemap.getTileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();
        canvas.draw(texture,
                    Color.WHITE,
                    origin.x,
                    origin.y,
                    x,
                    y,
                    0,
                    sclX,
                    sclY);
    }

}
