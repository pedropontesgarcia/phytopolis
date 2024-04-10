package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public abstract class Model {

    protected final Tilemap tilemap;
    protected final float textureSclInTiles;
    /**
     * Reference to texture origin
     */
    protected Vector2 origin;
    /**
     * Whether the object should be removed from the world on next pass
     */
    protected boolean toRemove;
    /**
     * Scale of the game objects
     */
    protected Vector2 scale = new Vector2(Gdx.graphics.getWidth() / 16f,
                                          Gdx.graphics.getHeight() / 9f);
    /**
     * The texture for the shape.
     */
    protected TextureRegion texture;

    public Model(Tilemap tilemap, float textureSclInTiles) {
        this.tilemap = tilemap;
        this.textureSclInTiles = textureSclInTiles;
    }

    public Texture getTexture() {
        return texture == null ? null : texture.getTexture();
    }

    public void setTexture(Texture texture) {
        this.texture = new FilmStrip(texture, 1, 1, 1);
        //        radius = animator.getRegionHeight() / 2.0f;
        origin = new Vector2(texture.getWidth() / 2.0f,
                             texture.getHeight() / 2.0f);
    }

    /**
     * Returns the position of this object (e.g. location of the center pixel)
     * <p>
     * The value returned is a reference to the position vector, which may be
     * modified freely.
     *
     * @return the position of this object
     */
    public abstract Vector2 getPosition();

    /**
     * Returns the x-coordinate of the object position (center).
     *
     * @return the x-coordinate of the object position
     */
    public abstract float getX();

    /**
     * Sets the x-coordinate of the object position (center).
     *
     * @param value the x-coordinate of the object position
     */
    public abstract void setX(float value);

    /**
     * Returns the y-coordinate of the object position (center).
     *
     * @return the y-coordinate of the object position
     */
    public abstract float getY();

    /**
     * Sets the y-coordinate of the object position (center).
     *
     * @param value the y-coordinate of the object position
     */
    public abstract void setY(float value);

    /**
     * Returns true if our object has been flagged for garbage collection
     * <p>
     * A garbage collected object will be removed from the physics world at
     * the next time step.
     *
     * @return true if our object has been flagged for garbage collection
     */
    public boolean isRemoved() {
        return toRemove;
    }

    /// Garbage Collection Methods

    /**
     * Sets whether our object has been flagged for garbage collection
     * <p>
     * A garbage collected object will be removed from the physics world at
     * the next time step.
     *
     * @param value whether our object has been flagged for garbage collection
     */
    public void markRemoved(boolean value) {
        toRemove = value;
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public abstract ModelType getType();

    /**
     * Updates the state of this object.
     * <p>
     * This method only is only intended to update values that change local state in
     * well-defined ways, like position or a cooldown value.  It does not handle
     * collisions (which are determined by the CollisionController).  It is
     * not intended to interact with other objects in any way at all.
     *
     * @param delta Number of seconds since last animation frame
     */
    public abstract void update(float delta);

    /**
     * Draws this object to the canvas
     * <p>
     * There is only one drawing pass in this application, so you can draw the objects
     * in any order.
     *
     * @param canvas The drawing context
     */
    public abstract void draw(GameCanvas canvas);

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public abstract void drawDebug(GameCanvas canvas);

    public enum ModelType {
        /**
         *
         */
        PLAYER,
        /**
         * A shell, which lives until it is destroyed by a star or bullet
         */
        LEAF,
        /**
         * A ship, which lives until it is destroyed by a shell
         */
        BRANCH,
        /**
         * A bullet, which is fired from the ship
         */
        DRONE,
        /**
         * A star, which is created by a shell explosion
         */
        FIRE, WATER, SUN, PLATFORM
    }

}
