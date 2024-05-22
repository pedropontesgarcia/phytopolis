package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Indicator extends Model {

    private final Texture texture;
    private final float UPWARD_SPEED = 1f;
    private final float LIFESPAN = 1f;
    private final Color color;
    private float tmr;

    public Indicator(float x, float y, Texture tx, Tilemap.TilemapParams tmp) {
        super(x, y, tmp, 1.5f);
        texture = tx;
        tmr = 0;
        color = new Color(Color.WHITE);
    }

    public Indicator(float x,
                     float y,
                     Texture tx,
                     Tilemap.TilemapParams tmp,
                     float scl) {
        super(x, y, tmp, 1.5f * scl);
        texture = tx;
        tmr = 0;
        color = new Color(Color.WHITE);
    }

    @Override
    public int getZIndex() {
        return 400;
    }

    @Override
    public ModelType getType() {
        return ModelType.INDICATOR;
    }

    @Override
    public void update(float delta) {
        setY(getY() + UPWARD_SPEED * delta);
        tmr += delta;
        if (tmr >= LIFESPAN) {
            tmr = LIFESPAN;
            markRemoved(true);
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        color.set(1, 1, 1, 1f - tmr / LIFESPAN);
        float width = tilemapParams.tileWidth() * textureSclInTiles;
        float height = tilemapParams.tileHeight() * textureSclInTiles;
        float sclX = width / texture.getWidth();
        float sclY = height / texture.getHeight();
        canvas.draw(texture,
                    color,
                    texture.getWidth() / 2f,
                    texture.getHeight() / 2f,
                    getX(),
                    getY(),
                    0,
                    sclX,
                    sclY);
    }

}
