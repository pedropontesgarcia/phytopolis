package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.physics.box2d.BodyDef;

public class Leaf extends BoxObject {

    /**
     * Creates a new Leaf object with the specified position and dimensions
     *
     * @param x x-position
     * @param y y-position
     * @param width width of the leaf
     * @param height height of the leaf
     */
    public Leaf(float x, float y, float width, float height) {
        super(x, y, width, height);
        bodyinfo.type = BodyDef.BodyType.StaticBody;
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

}
