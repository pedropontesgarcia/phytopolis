package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;

public class Water extends Resource {

    private final FilmStrip waterFilmstrip;

    public Water(float x, float y, FilmStrip wf) {
        super(x, y);

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
                (waterFilmstrip.getSize() - 1) * (1 - getRegenRatio())));

        canvas.draw(texture,
                    Color.WHITE,
                    origin.x,
                    origin.y,
                    getX() * drawScale.x,
                    getY() * drawScale.x,
                    getAngle(),
                    0.25f,
                    0.25f);
    }

}

