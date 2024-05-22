package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public abstract class Model {

    protected Tilemap.TilemapParams tilemapParams;
    protected float textureSclInTiles;
    /**
     * Object position (centered on the texture middle)
     */
    protected Vector2 position;
    /**
     * Reference to texture origin
     */
    protected Vector2 origin;
    /**
     * Whether the object should be removed from the world on next pass
     */
    protected boolean toRemove;
    /**
     * The texture for the shape.
     */
    protected FilmStrip texture;
    protected float animFrame;
    protected int zIndex;

    public enum ModelType {
        /**
         * The player
         */
        PLAYER,
        /**
         * A leaf
         */
        LEAF,
        /**
         * A branch
         */
        BRANCH,
        /**
         * A drone
         */
        DRONE,
        /**
         * A fire
         */
        FIRE,
        /**
         * Water resource
         */
        WATER,
        /**
         * Sun resource
         */
        SUN,
        /**
         * Platform
         */
        PLATFORM,
        /**
         * Full tile
         */
        TILE_FULL,
        /**
         * Tile without a top
         */
        TILE_NOTOP,
        /**
         * Bug hazard
         */
        BUG,
        /**
         * UI indicator
         */
        INDICATOR
    }

    public Model(float x,
                 float y,
                 Tilemap.TilemapParams tmp,
                 float textureSclInTiles) {
        position = new Vector2(x, y);
        this.tilemapParams = tmp;
        this.textureSclInTiles = textureSclInTiles;
        // Object has yet to be deactivated
        toRemove = false;
        zIndex = 0;
        animFrame = 0;
    }

    public Texture getTexture() {
        return texture == null ? null : texture.getTexture();
    }

    public void setTexture(Texture texture) {
        setFilmStrip(new FilmStrip(texture, 1, 1, 1));
    }

    public FilmStrip getFilmStrip() {
        return texture;
    }

    public void setFilmStrip(FilmStrip animation) {
        texture = animation;
        origin = new Vector2(texture.getRegionWidth() / 2.0f,
                             texture.getRegionHeight() / 2.0f);
    }

    /**
     * Returns the position of this object (e.g. location of the center pixel)
     * <p>
     * The value returned is a reference to the position vector, which may be
     * modified freely.
     *
     * @return the position of this object
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Returns the x-coordinate of the object position (center).
     *
     * @return the x-coordinate of the object position
     */
    public float getX() {
        return position.x;
    }

    /**
     * Sets the x-coordinate of the object position (center).
     *
     * @param value the x-coordinate of the object position
     */
    public void setX(float value) {
        position.x = value;
    }

    /**
     * Returns the y-coordinate of the object position (center).
     *
     * @return the y-coordinate of the object position
     */
    public float getY() {
        return position.y;
    }

    /**
     * Sets the y-coordinate of the object position (center).
     *
     * @param value the y-coordinate of the object position
     */
    public void setY(float value) {
        position.y = value;
    }

    /// Garbage Collection Methods

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

    public int getZIndex() {
        return zIndex;
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

}
