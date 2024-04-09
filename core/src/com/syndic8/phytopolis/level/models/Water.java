package com.syndic8.phytopolis.level.models;

import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;

public class Water extends Resource {
    public Water(float x, float y) {
        super(x, y);
    }

    @Override
    public ModelType getType() {
        return ModelType.WATER;
    }

    public void draw(GameCanvas canvas) {
        FilmStrip f = (FilmStrip) texture;
        f.setFrame(Math.round((f.getSize() - 1) * getRegenRatio()));
    }
}
