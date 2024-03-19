package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;

/**
 * Represents a fire with a specific location, duration, and burning state.
 */
public class Fire extends Hazard {

    /**
     * Indicates whether the fire is still burning.
     * It is true if the fire hasn't finished burning yet, otherwise false.
     */
    private boolean isBurning;

    /**
     * The fire's remaining burn duration.
     */
    private int duration;

    /**
     * The fire's location represented as a 2D vector.
     */
    private Vector2 location;

    /**
     * Returns the type of the model.
     *
     * @return ModelType.FIRE, representing the type of this object.
     */
    @Override
    public ModelType getType() {
        return ModelType.FIRE;
    }

    /**
     * Constructs a Fire object with a specified location and duration.
     *
     * @param location The initial location of the fire.
     * @param duration The initial burn duration of the fire.
     */
    public Fire(Vector2 location, int duration) {
        this.duration = duration;
        this.location = location;
        isBurning = true;
    }

    /**
     * Checks if the fire is still burning.
     *
     * @return true if the fire is burning, false otherwise.
     */
    public boolean isBurning() {
        return isBurning;
    }

    /**
     * Gets the current location of the fire.
     *
     * @return The current location of the fire.
     */
    public Vector2 getLocation() {
        return location;
    }

    /**
     * Sets a new location for the fire.
     *
     * @param location The new location of the fire.
     */
    public void setLocation(Vector2 location) {
        this.location = location;
    }

    /**
     * Gets the current burn duration of the fire.
     *
     * @return The current burn duration of the fire.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets a new burn duration for the fire.
     *
     * @param duration The new burn duration of the fire.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Renders the fire on the provided GameCanvas.
     *
     * @param canvas The GameCanvas on which to draw the fire.
     */
    public void draw(GameCanvas canvas, float x, float y) {
        canvas.draw(texture, Color.RED, origin.x, origin.y, x, y, 0f, .1f, .1f);
    }

}