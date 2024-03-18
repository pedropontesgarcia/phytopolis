package com.syndic8.phytopolis.level.models;

public class Drone extends Hazard {
    @Override
    public ModelType getType() {
        return ModelType.DRONE;
    }
}
