package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.syndic8.phytopolis.GameCanvas;

public abstract class CircleObject extends GameObject {
    /** Shape information for this circle */
    protected CircleShape shape;
    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;

    /**
     * Returns the radius of this circle
     *
     * @return the radius of this circle
     */
    public float getRadius() {
        return shape.getRadius();
    }

    /**
     * Sets the radius of this circle
     *
     * @param value  the radius of this circle
     */
    public void setRadius(float value) {
        shape.setRadius(value);
        markDirty(true);
    }

    /**
     * Creates a new circle at the origin.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param radius	The wheel radius
     */
    public CircleObject(float radius) {
        this(0, 0, radius);
    }

    /**
     * Creates a new circle object.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x 		Initial x position of the circle center
     * @param y  		Initial y position of the circle center
     * @param radius	The wheel radius
     */
    public CircleObject(float x, float y, float radius) {
        super(x,y);
        shape = new CircleShape();
        shape.setRadius(radius);
    }

    /**
     * Create new fixtures for this body, defining the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }

//    /**
//     * Draws the outline of the physics body.
//     *
//     * This method can be helpful for understanding issues with collisions.
//     *
//     * @param canvas Drawing context
//     */
//    public void drawDebug(GameCanvas canvas) {
//        canvas.drawPhysics(shape, Color.YELLOW,getX(),getY(),drawScale.x,drawScale.y);
//    }
}
