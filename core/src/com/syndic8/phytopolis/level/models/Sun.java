package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Sun extends Resource {

    public Sun(float x, float y, float w, float h, Tilemap tm, float texScl) {
        super(x, y, w, h, tm, texScl);
    }

    @Override
    public ModelType getType() {
        return ModelType.SUN;
    }

    public void draw(GameCanvas canvas) {
        FilmStrip f = (FilmStrip) texture;
        f.setFrame(isFull() ? f.getSize() - 1 : 0);
        float x = texture.getRegionWidth() / 2.0f;
        float y = texture.getRegionHeight() / 2.0f;
        canvas.draw(texture,
                    Color.WHITE,
                    x,
                    y,
                    getX() * drawScale.x,
                    getY() * drawScale.y,
                    getAngle(),
                    1,
                    1);
    }

}
