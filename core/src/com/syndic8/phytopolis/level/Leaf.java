package com.syndic8.phytopolis.level;

public class Leaf extends BoxObject {
    public Leaf(float x, float y, float width, float height) {
        super(x, y, width, height);
    }
    @Override
    public ModelType getType() {
        return ModelType.LEAF;
    }
}
