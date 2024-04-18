package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Leaf extends BoxObject {

    private final leafType type;

    private float animFrame;

    private static final float ANIMATION_SPEED = 1/6.0f;

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

    //    @Override
    //    public boolean activatePhysics(World world) {
    //        boolean success = super.activatePhysics(world);
    //        if (success) body.setUserData(ModelType.LEAF);
    //        return success;
    //    }

    public void setFilmStrip(FilmStrip f) {
        this.texture = f;
    }

    public FilmStrip getFilmStrip() {
        return (FilmStrip) this.texture;
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
        if (animFrame < getFilmStrip().getSize() - 1) {
            animFrame += ANIMATION_SPEED;
        } else if (animFrame >= getFilmStrip().getSize()) {
            animFrame = getFilmStrip().getSize() - 1;
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
        getFilmStrip().setFrame((int)animFrame);
        canvas.draw(texture,
                Color.WHITE,
                x,
                y,
                getX(),
                getY(),
                0,
                sclX,
                sclY);
    }

    /**
     * enum containing possible leaf types
     */
    public enum leafType {NORMAL, BOUNCY}

}
