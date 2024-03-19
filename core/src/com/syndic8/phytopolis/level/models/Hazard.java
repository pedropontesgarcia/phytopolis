package com.syndic8.phytopolis.level.models;

public abstract class Hazard extends CircleObject {
    public Hazard() {
        super(30);
    }

    @Override
    public abstract ModelType getType();
}
