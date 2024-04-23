package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.util.Tilemap;

/**
 * Represents a drone in the game, which is a type of hazard.
 * Drones have a location and a state indicating whether they have collided with a plant.
 */
public class Drone extends Hazard {

    /**
     * Indicates whether the drone is still alive (has not collided with the plant).
     * It is true if the drone hasn't collided with the plant yet, otherwise false.
     */
    private final boolean isAlive;

    /**
     * Time til explosion.
     */
    private int timer;

    /**
     * Constructs a Drone object with a specified location.
     * The drone is initially alive upon creation.
     *
     * @param location The initial location of the drone.
     */
    public Drone(Vector2 pos, Vector2 location,
                 int timeTilExplosion,
                 Tilemap tm,
                 float texScl) {
        super(tm, texScl, pos, location, timeTilExplosion);
        isAlive = true;
    }

    /**
     * Returns the type of the model.
     *
     * @return ModelType.DRONE, representing the type of this object.
     */
    @Override
    public ModelType getType() {
        return ModelType.DRONE;
    }

    /**
     * Checks if the drone is still alive (has not collided with a plant).
     *
     * @return true if the drone is alive, false otherwise.
     */
    public boolean isAlive() {
        return isAlive;
    }

    public void update(float delta) {

    }

}
