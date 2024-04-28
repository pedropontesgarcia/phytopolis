package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Bug extends Hazard {

    private final int NUM_FRAMES = 6;

    /**
     * Constructs a Bug object with a specified location and duration.
     *
     */
    public Bug(Vector2 pos, Vector2 location, int duration, Tilemap tm, float texScl) {
        super(tm, texScl, pos.set(pos.x, pos.y + 0.5f), location, duration);
    }
    @Override
    public ModelType getType() {
        return ModelType.BUG;
    }

    @Override
    public void draw(GameCanvas canvas) {
        float width = tilemap.getTileWidth() * textureSclInTiles;
        float height = tilemap.getTileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();

        getFilmStrip().setFrame((int) animFrame);
        float x = getFilmStrip().getRegionWidth() / 2.0f;
        float y = getFilmStrip().getRegionHeight() / 2.0f;
        canvas.draw(texture,
                Color.WHITE,
                x,
                y,
                getX(),
                getY(),
                0,
                sclX,
                sclY);
    }

    @Override
    public void update(float delta) {
        if (animFrame < NUM_FRAMES) {
            animFrame += animationSpeed;
        } else {
            animFrame = 3;
        }
    }
}
