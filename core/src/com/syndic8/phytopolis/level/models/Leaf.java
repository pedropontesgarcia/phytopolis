package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Leaf extends BoxObject {

    private final leafType type;

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

    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            float width = tilemap.getTileWidth() * textureSclInTiles;
            float height = tilemap.getTileHeight() * textureSclInTiles;
            float sclX = width / texture.getRegionWidth();
            float sclY = height / texture.getRegionHeight();
            canvas.draw(texture,
                        Color.WHITE,
                        origin.x,
                        origin.y,
                        getX(),
                        getY(),
                        getAngle(),
                        sclX,
                        sclY);
        }
    }

    /**
     * enum containing possible leaf types
     */
    public enum leafType {NORMAL, BOUNCY}

}
