package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Sun extends Resource {

    private static final float SPIN_RATE = 2f;
    private static final float TOLERANCE = 2f;
    //private final FilmStrip sunFilmstrip;
    private final Color color;
    private final Texture sunCircle;
    private final Texture sunRay;
    private final Texture sunSwirl;
    private float maxLeafHeight = -1;
    private float angle;
    private boolean fading;

    public Sun(float x,
               float y,
               float w,
               float h,
               Texture sc,
               Texture sr,
               Texture ss,
               Tilemap.TilemapParams tmp,
               float texScl) {
        super(x, y, w, h, tmp, texScl);
        bodyinfo.gravityScale = 0;
        //sunFilmstrip = sf;
        color = new Color(1.0F, 1.0F, 1.0F, 1.0F);
        setTexture(sc);
        this.angle = 0;
        this.sunCircle = sc;
        this.sunRay = sr;
        this.sunSwirl = ss;
        fading = false;
    }

    @Override
    public ModelType getType() {
        return ModelType.SUN;
    }

    public void update(float dt, boolean belowLeaf) {
        if (maxLeafHeight == -1 && belowLeaf) {
            startFade(getY());
        }
        if (belowScreen() || maxLeafHeight - getY() >= TOLERANCE) {
            clear();
        }
        angle += dt * SPIN_RATE;
    }

    public void startFade(float f) {
        maxLeafHeight = f;
        fading = true;
    }

    public boolean belowScreen() {
        return getY() + getRadius() < 0;
    }

    public void clear() {
        bodyinfo.type = BodyDef.BodyType.StaticBody;
        markRemoved(true);
    }

    public boolean isFading() {
        return fading;
    }

    public void draw(GameCanvas canvas) {
        setVX(0);
        setVY(-0.5f);
        float sclX = width / sunCircle.getWidth();
        float sclY = height / sunCircle.getHeight();
        if (maxLeafHeight != -1) {
            color.set(1.0f,
                      1.0f,
                      1.0f,
                      1.0f - Math.max(0,
                                      maxLeafHeight - getY() - TOLERANCE + 1f));
        }
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
