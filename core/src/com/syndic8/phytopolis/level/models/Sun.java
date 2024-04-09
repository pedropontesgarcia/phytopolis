package com.syndic8.phytopolis.level.models;

import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;

public class Sun extends Resource {
    public Sun(float x, float y) {
        super(x, y);
    }
    @Override
    public ModelType getType() {
        return ModelType.SUN;
    }

    public void draw(GameCanvas canvas) {
        FilmStrip f = (FilmStrip) texture;
        f.setFrame(isFull() ? f.getSize() - 1 : 0);
    }
}
