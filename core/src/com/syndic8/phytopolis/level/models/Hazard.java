package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.syndic8.phytopolis.GameCanvas;

public abstract class Hazard extends CircleObject {
    public Hazard() {
        super(30);
    }

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
