package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Leaf extends BoxObject {

    private static final float ANIMATION_SPEED = 1 / 6.0f;
    private final leafType type;
    private float health;
    private int healthMark;
    private boolean beingEaten;

    /**
     * Creates a new Leaf object with the specified position and dimensions
     *
     * @param x      x-position
     * @param y      y-position
     * @param width  width of the leaf
     * @param height height of the leaf
     * @param type
     */
    public Leaf(float x,
                float y,
                float width,
                float height,
                leafType type,
                Tilemap tm,
                float texScl) {
        super(x, y, width, height, tm, texScl);
        bodyinfo.type = BodyDef.BodyType.StaticBody;
        this.type = type;
        zIndex = 2;
        health = 5;
        healthMark = 5;
        beingEaten = false;
    }

    /**
     * returns the type of this leaf
     *
     * @return the type of this leaf
     */
    public leafType getLeafType() {
        return type;
    }

    @Override
    public ModelType getType() {
        return ModelType.LEAF;
    }

    public boolean fullyEaten() {
        return health <= 0;
    }

    public void setBeingEaten(boolean value) {
        beingEaten = value;
    }

    public boolean healthBelowMark() {
        if (health < healthMark) {
            healthMark--;
            return true;
        }
        return false;
    }

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
    public void update(float delta) {
        if (beingEaten) {
            health -= delta;
        }
        if (animFrame < 4) {
            animFrame += ANIMATION_SPEED;
        } else if (health < 5 && health > 0) {
            animFrame = 4 + (5 - health);
        }
    }

    /**
     * Draws this object to the canvas
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas) {
        float width = tilemap.getTileWidth() * textureSclInTiles;
        float height = tilemap.getTileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();
        float x = texture.getRegionWidth() / 2.0f;
        float y = texture.getRegionHeight() / 2.0f;
        getFilmStrip().setFrame((int) animFrame);
        canvas.draw(texture, Color.WHITE, x, y, getX(), getY(), 0, sclX, sclY);
    }

    /**
     * enum containing possible leaf types
     */
    public enum leafType {NORMAL, BOUNCY, NORMAL1, NORMAL2}

}
