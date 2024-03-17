package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;

public abstract class Model {
    public enum ModelType {
        /** */
        PLAYER,
        /** A shell, which lives until it is destroyed by a star or bullet */
        LEAF,
        /** A ship, which lives until it is destroyed by a shell */
        BRANCH,
        /** A bullet, which is fired from the ship */
        DRONE,
        /** A star, which is created by a shell explosion */
        FIRE,
        WATER,
        SUN
    }
    /** Reference to texture origin */
    protected Vector2 origin;
    /** CURRENT image for this object. May change over time. */
    protected FilmStrip animator;

    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture,1,1,1);
//        radius = animator.getRegionHeight() / 2.0f;
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }

    public Texture getTexture() {
        return animator == null ? null : animator.getTexture();
    }

    /**
     * Returns the position of this object (e.g. location of the center pixel)
     *
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
     * Returns the type of this object.
     *
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public abstract ModelType getType();

    /**
     * Updates the state of this object.
     *
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
     *
     * There is only one drawing pass in this application, so you can draw the objects
     * in any order.
     *
     * @param canvas The drawing context
     */
    public abstract void draw(GameCanvas canvas);
}
