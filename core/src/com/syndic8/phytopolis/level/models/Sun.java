package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Sun extends Resource {

    private static final float SPIN_RATE = 0.025f;
    //private final FilmStrip sunFilmstrip;
    private Color color;
    private float maxLeafHeight = -1;
    private float angle;
    private Texture sunCircle;
    private Texture sunRay;
    private Texture sunSwirl;

    public Sun(float x,
               float y,
               float w,
               float h,
               Texture sc,
               Texture sr,
               Texture ss,
               Tilemap tm,
               float texScl) {
        super(x, y, w, h, tm, texScl);
        bodyinfo.gravityScale = 0;
        //sunFilmstrip = sf;
        color = new Color(1.0F, 1.0F, 1.0F, 1.0F);
        setTexture(sc);
        this.angle = 0;
        this.sunCircle = sc;
        this.sunRay = sr;
        this.sunSwirl = ss;
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
        float sclX = width / sunCircle.getWidth();
        float sclY = height / sunCircle.getHeight();
        if (maxLeafHeight != -1) {
            color.set(1.0f, 1.0f, 1.0f, 1.0f - Math.max(0, maxLeafHeight - getY()));
        }
        angle += SPIN_RATE;
        canvas.draw(sunCircle,
                    color,
                    origin.x,
                    origin.y,
                    getX(),
                    getY(),
                    getAngle() + angle,
                    sclX,
                    sclY);
        canvas.draw(sunRay,
                color,
                origin.x,
                origin.y,
                getX(),
                getY(),
                getAngle() + angle,
                sclX,
                sclY);
        canvas.draw(sunSwirl,
                color,
                origin.x,
                origin.y,
                getX(),
                getY(),
                getAngle() - angle,
                sclX,
                sclY);
    }

}
