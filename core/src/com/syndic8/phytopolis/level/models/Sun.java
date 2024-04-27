package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Sun extends Resource {

    private final FilmStrip sunFilmstrip;
    private Color color;
    private float maxLeafHeight = -1;

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
        color = new Color(1.0F, 1.0F, 1.0F, 1.0F);
        setTexture(sf);
    }

    public boolean belowScreen() {
        return getY() + getRadius() < 0;
    }

    public void clear() {
        bodyinfo.type = BodyDef.BodyType.StaticBody;
        markRemoved(true);
    }

    public void startFade(float f) {
        maxLeafHeight = f;
    }

    @Override
    public ModelType getType() {
        return ModelType.SUN;
    }

    public void update(boolean belowLeaf) {
        if (maxLeafHeight == -1 && belowLeaf) {
            startFade(getY());
        }
        if (belowScreen() || maxLeafHeight - getY() >= 1) {
            clear();
        }
    }

    public void draw(GameCanvas canvas) {
        setVX(0);
        setVY(-0.5f);
        float sclX = width / sunFilmstrip.getRegionWidth();
        float sclY = height / sunFilmstrip.getRegionHeight();
        if (maxLeafHeight != -1) {
            color.set(1.0f, 1.0f, 1.0f, 1.0f - Math.max(0, maxLeafHeight - getY()));
        }
        canvas.draw(sunFilmstrip,
                    color,
                    origin.x,
                    origin.y,
                    getX(),
                    getY(),
                    getAngle(),
                    sclX,
                    sclY);
    }

}
