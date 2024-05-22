package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public abstract class Hazard extends BoxObject {

    private final int maxTimer;
    /**
     * The current location (index-wise) of the hazard.
     */
    protected Vector2 location;
    /**
     * Time until trigger event
     */
    private int timer;
    private TextureRegion prevTexture;

    /**
     * Creates a hazard object.
     */
    public Hazard(Tilemap.TilemapParams tmp,
                  float texScl,
                  Vector2 pos,
                  Vector2 location,
                  int timer) {
        super(pos.x, pos.y, 0.2f, 0.1f, tmp, texScl);
        this.location = location;
        this.timer = timer;
        maxTimer = timer;
        zIndex = 4;
    }

    /**
     * Gets the current location of the hazard.
     *
     * @return The current location of the hazard.
     */
    public Vector2 getLocation() {
        return location;
    }

    /**
     * Sets a new location for the hazard.
     *
     * @param location The new location of the hazard.
     */
    public void setLocation(Vector2 location) {
        this.location = location;
    }

    public int getMaxTimer() {
        return maxTimer;
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    @Override
    public abstract ModelType getType();

    /**
     * Gets the time till explosion.
     *
     * @return The current location of the drone.
     */
    public int getTimer() {
        return timer;
    }

    /**
     * Sets the time till explosion.
     */
    public void setTimer(int time) {
        timer = time;
    }

    public boolean tick() {
        timer--;
        return timer == 0;
    }

    public void update(float dt) {

    }

    /**
     * Draws the hazard on the provided GameCanvas.
     *
     * @param canvas The GameCanvas on which to draw.
     *               //     * @param x      The x position at which to draw.
     *               //     * @param y      The y position at which to draw.
     */
    public void draw(GameCanvas canvas) {
        float width = tilemapParams.tileWidth() * textureSclInTiles;
        float height = tilemapParams.tileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();

        if (texture != null) {
            canvas.draw(texture,
                        Color.WHITE,
                        origin.x,
                        origin.y,
                        getX(),
                        getY(),
                        0,
                        sclX,
                        sclY);
        }

    }

    public void setPreviousTexture(TextureRegion warningTex) {
        prevTexture = warningTex;
    }

    public TextureRegion previousTex() {
        return prevTexture;
    }

}
