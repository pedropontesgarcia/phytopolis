package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.util.Tilemap;

/**
 * Represents a fire with a specific location, duration, and burning state.
 */
public class Fire extends Hazard {

    /**
     * Indicates whether the fire is still burning.
     * It is true if the fire hasn't finished burning yet, otherwise false.
     */
    private final boolean isBurning;

    /**
     * Constructs a Fire object with a specified location and duration.
     *
     * @param location The initial location of the fire.
     * @param duration The initial burn duration of the fire.
     */
    public Fire(Vector2 pos, Vector2 location, int duration, Tilemap tm, float texScl) {
        super(tm, texScl, pos, location, duration);
        isBurning = true;
    }

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
     * Checks if the fire is still burning.
     *
     * @return true if the fire is burning, false otherwise.
     */
    public boolean isBurning() {
        return isBurning;
    }

    public void update(float delta) {}

}