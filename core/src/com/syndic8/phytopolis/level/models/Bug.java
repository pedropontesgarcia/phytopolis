package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Bug extends Hazard {

    private final int NUM_NORMAL_FRAMES = 6;
    private final int NUM_TOTAL_FRAMES = 9;
    private final float ANIMATION_SPEED = 10.0f / 3.0f;
    private boolean despawning;
    private boolean doneAnim;
    private int zoneIndex;

    /**
     * Constructs a Bug object with a specified location and duration.
     */
    public Bug(Vector2 pos,
               Vector2 location,
               int duration,
               Tilemap.TilemapParams tmp,
               float texScl) {
        super(tmp, texScl, pos.set(pos.x, pos.y + 0.4f), location, duration);
        setGravityScale(0);
        despawning = false;
        doneAnim = false;
    }

    public int getZoneIndex() {
        return zoneIndex;
    }

    public void setZoneIndex(int value) {
        zoneIndex = value;
    }

    @Override
    public ModelType getType() {
        return ModelType.BUG;
    }

    @Override
    public void update(float dt) {
        if (despawning) {
            if (animFrame < NUM_NORMAL_FRAMES) {
                animFrame = NUM_NORMAL_FRAMES;
            } else {
                animFrame += dt * ANIMATION_SPEED;
            }
            if (animFrame >= NUM_TOTAL_FRAMES) {
                doneAnim = true;
                animFrame = NUM_TOTAL_FRAMES - 1;
            }
        } else {
            if (animFrame < NUM_NORMAL_FRAMES) {
                animFrame += dt * ANIMATION_SPEED;
            } else {
                animFrame = 3;
            }
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        float width = tilemapParams.tileWidth() * textureSclInTiles;
        float height = tilemapParams.tileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();

        getFilmStrip().setFrame((int) animFrame);
        float x = getFilmStrip().getRegionWidth() / 2.0f;
        float y = getFilmStrip().getRegionHeight() / 2.0f;
        canvas.draw(texture, Color.WHITE, x, y, getX(), getY(), 0, sclX, sclY);
    }

    public void setDespawning(boolean value) {
        despawning = value;
    }

    public boolean getDoneAnim() {
        return doneAnim;
    }

}
