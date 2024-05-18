package com.syndic8.phytopolis.levelselect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.syndic8.phytopolis.GameCanvas;

public class LevelBox {

    private final boolean debugOn = false;
    private final float x;
    private final float y;
    private final float width = 3f;
    private final float height = 3f;
    private Texture texture;
    private boolean selected;

    public LevelBox(float x, float y) {
        this.x = x;
        this.y = y;
        this.selected = false;
    }

    public void draw(GameCanvas canvas) {
        if (debugOn) {
            Color c = Color.WHITE;
            if (selected) c = Color.BLACK;
            float scale = 0.01f;
            canvas.draw(texture,
                        c,
                        texture.getWidth() / scale,
                        texture.getHeight() / scale,
                        x,
                        y,
                        0f,
                        0.01f,
                        0.01f);
            canvas.draw(texture,
                        c,
                        texture.getWidth() / scale,
                        texture.getHeight() / scale,
                        x + width,
                        y,
                        0f,
                        0.01f,
                        0.01f);
            canvas.draw(texture,
                        c,
                        texture.getWidth() / scale,
                        texture.getHeight() / scale,
                        x + width,
                        y + height,
                        0f,
                        0.01f,
                        0.01f);
            canvas.draw(texture,
                        c,
                        texture.getWidth() / scale,
                        texture.getHeight() / scale,
                        x,
                        y + height,
                        0f,
                        0.01f,
                        0.01f);
        }
    }

    public void setTexture(Texture rs) {
        texture = rs;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean value) {
        selected = value;
    }

    /**
     * check to see if a given set of coordinates are in this box
     */
    public boolean inBounds(float xArg, float yArg) {
        boolean xIn = xArg >= x && xArg <= x + width;
        boolean yIn = yArg >= y && yArg <= y + height;
        return xIn && yIn;
    }

}
