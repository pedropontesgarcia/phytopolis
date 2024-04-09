package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;

public class Leaf extends BoxObject {
    /**
     * enum containing possible leaf types
     */
    public enum leafType {NORMAL, BOUNCY}
    private leafType type;

    /**
     * Creates a new Leaf object with the specified position and dimensions
     *
     * @param x      x-position
     * @param y      y-position
     * @param width  width of the leaf
     * @param height height of the leaf
     * @param type
     */
    public Leaf(float x, float y, float width, float height, leafType type) {
        super(x, y, width, height);
        bodyinfo.type = BodyDef.BodyType.StaticBody;
        this.type = type;
    }

    /**
     * returns the type of this leaf
     * @return the type of this leaf
     */
    public leafType getLeafType(){
        return type;
    }

    //    @Override
    //    public boolean activatePhysics(World world) {
    //        boolean success = super.activatePhysics(world);
    //        if (success) body.setUserData(ModelType.LEAF);
    //        return success;
    //    }

    @Override
    public ModelType getType() {
        return ModelType.LEAF;
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            switch (type){
                case NORMAL:
                    canvas.draw(texture,
                            Color.WHITE,
                            origin.x,
                            origin.y,
                            getX() * drawScale.x,
                            getY() * drawScale.x,
                            getAngle(),
                            1,
                            1);
                    break;
                case BOUNCY:
                    canvas.draw(texture,
                            Color.WHITE,
                            origin.x,
                            origin.y,
                            getX() * drawScale.x,
                            getY() * drawScale.x,
                            getAngle(),
                            0.21f,
                            0.21f);
                    break;
            }
        }
    }
}
