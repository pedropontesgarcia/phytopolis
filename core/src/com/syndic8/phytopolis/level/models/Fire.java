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
    private final float growthRate = 0.0001f; // Growth rate per second
    private float size = .4f; // Initial size
    private float elapsedTime = 0.0f; // Elapsed time since last update

    /**
     * Constructs a Fire object with a specified location and duration.
     *
     * @param location The initial location of the fire.
     * @param duration The initial burn duration of the fire.
     */
    public Fire(Vector2 pos,
                Vector2 location,
                int duration,
                Tilemap tm,
                float texScl) {
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

    public void update(float delta) {
        if (animFrame < NUM_FRAMES) {
            animFrame += animationSpeed;
        }
        if (animFrame >= NUM_FRAMES) {
            animFrame = 0;
        }
        // Update elapsed time
        elapsedTime += delta;
        // Calculate size increment based on growth rate and elapsed time
        float sizeIncrement = growthRate * elapsedTime;
        // Limit size to maximum of 1f
        size = Math.min(size + sizeIncrement, 1f);
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
                    sclX * size,
                    sclY * size);
    }

}