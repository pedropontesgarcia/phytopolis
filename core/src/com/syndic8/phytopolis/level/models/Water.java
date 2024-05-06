package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Water extends Resource {

    private static final int REGEN_DELAY = 15;
    private static final int MAX_REGEN = 100;
    private final FilmStrip waterFilmstrip;
    private int currRegen;
    private int currDelay;
    private float animFrame;

    public Water(float x,
                 float y,
                 float w,
                 float h,
                 FilmStrip wf,
                 Tilemap tm,
                 float texScl) {
        super(x, y, w, h, tm, texScl);
        currRegen = MAX_REGEN;
        currDelay = 0;
        bodyinfo.type = BodyDef.BodyType.StaticBody;
        waterFilmstrip = wf;
        setFilmStrip(wf);
        animFrame = 13;
    }

    public boolean isFull() {
        return currRegen == MAX_REGEN;
    }

    public void clear() {
        currRegen = 0;
        currDelay = 0;
    }

    public void regenerate() {
        if (currRegen < MAX_REGEN) {
            currDelay++;
            if (currDelay >= REGEN_DELAY) {
                currDelay -= REGEN_DELAY;
                currRegen++;

            }
        }
    }

    @Override
    public ModelType getType() {
        return ModelType.WATER;
    }

    public void update(float dt) {
        if (isFull()) {
            animFrame += dt * 5;
            if (animFrame >= 26) {
                animFrame = 13;
            }
            waterFilmstrip.setFrame((int)animFrame);
        } else {
            waterFilmstrip.setFrame(Math.round(
                    (waterFilmstrip.getSize() - 1) * (getRegenRatio())));
            animFrame = 13;
        }
    }

    public void draw(GameCanvas canvas) {
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

    public float getRegenRatio() {
        return (float) currRegen / MAX_REGEN;
    }

}

