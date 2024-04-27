package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.util.Tilemap;

public class Bug extends Hazard {

    /**
     * Constructs a Bug object with a specified location and duration.
     *
     */
    public Bug(Vector2 pos, Vector2 location, int duration, Tilemap tm, float texScl) {
        super(tm, texScl, pos, location, duration);
    }
    @Override
    public ModelType getType() {
        return ModelType.BUG;
    }

    @Override
    public void update(float delta) {

    }
}
