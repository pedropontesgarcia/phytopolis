package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.syndic8.phytopolis.GameCanvas;

public abstract class Hazard extends CircleObject {
    private static final int HAZARD_RADIUS = 30;
    /**
     * Creates an object with the desired hazard radius.
     */
    public Hazard() {
        super(HAZARD_RADIUS);
    }

    /**
     * Returns the type of this object.
     *
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
     * @param x The x position at which to draw.
     * @param y The y position at which to draw.
     */
    public void draw(GameCanvas canvas, float x, float y) {
        canvas.draw(texture, Color.RED, origin.x, origin.y, x, y, 0f, .1f, .1f);
    }
}
