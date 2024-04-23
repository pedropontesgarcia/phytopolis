package com.syndic8.phytopolis.levelselect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.syndic8.phytopolis.GameCanvas;

public class LevelBox {
    private PolygonRegion rect;
    private final boolean debugOn = false;
    private float x;
    private float y;
    private final float width = 3f;
    private final float height = 3f;
    private Texture texture;
    private boolean selected;

    public LevelBox(float x, float y){
        this.x = x;
        this.y = y;
        this.selected = false;
    }

    public void draw(GameCanvas canvas){
        if (debugOn) {
            Color c = Color.WHITE;
            if(selected) c = Color.BLACK;
            canvas.draw(texture, c,texture.getWidth()/2f, texture.getHeight()/2f, x, y,0f, 0.01f, 0.01f);
            canvas.draw(texture, c,texture.getWidth()/2f, texture.getHeight()/2f, x + width, y,0f, 0.01f, 0.01f);
            canvas.draw(texture, c,texture.getWidth()/2f, texture.getHeight()/2f, x + width, y + height,0f, 0.01f, 0.01f);
            canvas.draw(texture, c,texture.getWidth()/2f, texture.getHeight()/2f, x, y + height,0f, 0.01f, 0.01f);
            //System.out.println("X: " + x + " Y: " + y);
        }
    }

    public void setTexture(Texture rs) {
        texture = rs;
    }
    public boolean getSelected(){
        return selected;
    }

    public void setSelected(boolean value){
        selected = value;
    }

    /**
     * check to see if a given set of coordinates are in this box
     */
    public boolean inBounds(float xArg, float yArg){
        boolean xIn = xArg >= x && xArg <= x + width;
        boolean yIn = yArg >= y && yArg <= y + height;
        return xIn && yIn;
    }
}
