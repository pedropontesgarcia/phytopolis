package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Water extends Resource {

    private final FilmStrip waterFilmstrip;

    public Water(float x,
                 float y,
                 float w,
                 float h,
                 FilmStrip wf,
                 Tilemap tm,
                 float texScl) {
        super(x, y, w, h, tm, texScl);

        bodyinfo.type = BodyDef.BodyType.StaticBody;
        waterFilmstrip = wf;
        setTexture(wf);
    }

    @Override
    public ModelType getType() {
        return ModelType.WATER;
    }

    public void draw(GameCanvas canvas) {
        waterFilmstrip.setFrame(Math.round(
                (waterFilmstrip.getSize() - 1) * (getRegenRatio())));
        float sclX = width / waterFilmstrip.getRegionWidth();
        float sclY = height / waterFilmstrip.getRegionHeight();
        canvas.draw(waterFilmstrip,
                    Color.WHITE,
                    origin.x,
                    origin.y,
                    getX(),
                    getY(),
                    getAngle(),
                    sclX,
                    sclY);
    }

}

