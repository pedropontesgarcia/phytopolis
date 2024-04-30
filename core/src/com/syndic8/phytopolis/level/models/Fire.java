package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

/**
 * Represents a fire with a specific location, duration, and burning state.
 */
public class Fire extends Hazard {

    private final int NUM_FRAMES = 16;

    /**
     * Constructs a Fire object with a specified location and duration.
     *
     * @param location The initial location of the fire.
     * @param duration The initial burn duration of the fire.
     */
    public Fire(Vector2 pos, Vector2 location, int duration, Tilemap tm, float texScl) {
        super(tm, texScl, pos, location, duration);
    }

    /**
     * Returns the type of the model.
     *
     * @return ModelType.FIRE, representing the type of this object.
     */
    @Override
    public ModelType getType() {
        return ModelType.FIRE;
    }

    public void draw(GameCanvas canvas) {
        float width = tilemap.getTileWidth() * textureSclInTiles;
        float height = tilemap.getTileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();

        getFilmStrip().setFrame((int) animFrame);
        float x = getFilmStrip().getRegionWidth() / 2.0f;
        float y = getFilmStrip().getRegionHeight() / 2.0f;
        canvas.draw(getFilmStrip(),
                Color.WHITE,
                x,
                y,
                getX(),
                getY(),
                0,
                sclX,
                sclY);
    }

    public void update(float delta) {
        if (animFrame < NUM_FRAMES) {
            animFrame += animationSpeed;
        }
        if (animFrame >= NUM_FRAMES) {
            animFrame = 0;
        }
    }

}