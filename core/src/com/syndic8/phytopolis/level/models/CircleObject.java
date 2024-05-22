package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.syndic8.phytopolis.util.Tilemap;

public abstract class CircleObject extends GameObject {

    /**
     * Shape information for this circle
     */
    protected CircleShape shape;
    /**
     * A cache value for the fixture (for resizing)
     */
    private Fixture geometry;

    /**
     * Creates a new circle object.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x      Initial x position of the circle center
     * @param y      Initial y position of the circle center
     * @param radius The wheel radius
     */
    public CircleObject(float x,
                        float y,
                        float radius,
                        Tilemap.TilemapParams tmp,
                        float texScl) {
        super(x, y, tmp, texScl);
        shape = new CircleShape();
        shape.setRadius(radius);
    }

    /**
     * Returns the radius of this circle
     *
     * @return the radius of this circle
     */
    public float getRadius() {
        return shape.getRadius();
    }

    /**
     * Create new fixtures for this body, defining the shape
     * <p>
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
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }

}
