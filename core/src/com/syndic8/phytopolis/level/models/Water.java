package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
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
        float x = texture.getRegionWidth()/2.0f;
        float y = texture.getRegionHeight()/2.0f;
        canvas.draw(
                texture,
                Color.WHITE,
                x,
                y,
                getX()*drawScale.x,
                getY()*drawScale.y,
                getAngle(),
                1, 1
        );
    }
}
