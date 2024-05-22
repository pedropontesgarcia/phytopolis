package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.util.Tilemap;

/**
 * Represents a drone in the game, which is a type of hazard.
 * Drones have a location and a state indicating whether they have collided with a plant.
 */
public class Drone extends Hazard {

    /**
     * Constructs a Drone object with a specified location.
     * The drone is initially alive upon creation.
     *
     * @param location The initial location of the drone.
     */
    public Drone(Vector2 pos,
                 Vector2 location,
                 int timeTilExplosion,
                 Tilemap.TilemapParams tmp,
                 float texScl) {
        super(tmp, texScl, pos, location, timeTilExplosion);
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

    public void update(float delta) {

    }

}
