package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;

/**
 * Represents a drone in the game, which is a type of hazard.
 * Drones have a location and a state indicating whether they have collided with a plant.
 */
public class Drone extends Hazard {

    /**
     * Indicates whether the drone is still alive (has not collided with the plant).
     * It is true if the drone hasn't collided with the plant yet, otherwise false.
     */
    private boolean isAlive;

    /**
     * The current location of the drone.
     */
    private Vector2 location;

    /**
     * Time til explosion.
     */
    private int timer;

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
     * Constructs a Drone object with a specified location.
     * The drone is initially alive upon creation.
     *
     * @param location The initial location of the drone.
     */
    public Drone(Vector2 location, int timeTilExplosion) {
        this.timer = timeTilExplosion;
        this.location = location;
        isAlive = true;
    }

    /**
     * Checks if the drone is still alive (has not collided with a plant).
     *
     * @return true if the drone is alive, false otherwise.
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Gets the current location of the drone.
     *
     * @return The current location of the drone.
     */
    public Vector2 getLocation() {
        return location;
    }

    /**
     * Sets a new location for the drone.
     *
     * @param location The new location of the drone.
     */
    public void setLocation(Vector2 location) {
        this.location = location;
    }

    /**
     * Gets the time till explosion.
     *
     * @return The current location of the drone.
     */
    public int getTimer() {
        return timer;
    }

    /**
     * Sets the time till explosion.
     */
    public void setTimer(int time) {
        timer = time;
    }

    /**
     * Renders the drone on the provided GameCanvas.
     *
     * @param canvas The GameCanvas on which to draw the drone.
     */
    public void draw(GameCanvas canvas, float x, float y) {
        canvas.draw(texture, Color.RED, origin.x, origin.y, x, y, 0f, .1f, .1f);
    }
}
