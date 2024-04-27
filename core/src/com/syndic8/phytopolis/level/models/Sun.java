package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Sun extends Resource {

    private final FilmStrip sunFilmstrip;

    public Sun(float x,
               float y,
               float w,
               float h,
               FilmStrip sf,
               Tilemap tm,
               float texScl) {
        super(x, y, w, h, tm, texScl);
        bodyinfo.gravityScale = 0;
        sunFilmstrip = sf;
        setTexture(sf);
    }

    public boolean belowScreen() {
        return getY() + getRadius() < 0;
    }

    public void clear() {
        bodyinfo.type = BodyDef.BodyType.StaticBody;
        markRemoved(true);
    }

    @Override
    public ModelType getType() {
        return ModelType.SUN;
    }

    public void draw(GameCanvas canvas) {
        setVX(0);
        setVY(-0.5f);
        float sclX = width / sunFilmstrip.getRegionWidth();
        float sclY = height / sunFilmstrip.getRegionHeight();
        canvas.draw(sunFilmstrip,
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
