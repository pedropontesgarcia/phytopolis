package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Tile extends Model {

    private final ModelType type;
    private final Texture texture;
    private final float width;
    private final float height;
    private PolygonObject collider;

    public Tile(Tilemap.TilemapParams tmp,
                Vector2 pos,
                boolean collideTop,
                Texture tx) {
        super(pos.x, pos.y, tmp, 1);
        type = (collideTop ? ModelType.TILE_FULL : ModelType.TILE_NOTOP);
        texture = tx;
        width = tmp.tileWidth();
        height = tmp.tileHeight();
    }

    public void addCollider(float[] pts) {
        collider = new PolygonObject(pts, 0, 0, tilemapParams, 1);
        collider.setBodyType(BodyDef.BodyType.StaticBody);
        collider.setDensity(0);
        collider.setFriction(0);
        collider.setRestitution(0);
    }

    public void fixColliderUserData() {
        collider.getBody().setUserData(this);
    }

    @Override
    public ModelType getType() {
        return type;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(texture,
                    Color.WHITE,
                    position.x,
                    position.y,
                    width,
                    height);
    }

    //    @Override
    //    public void drawDebug(GameCanvas canvas) {
    //
    //    }

    public PolygonObject getCollider() {
        return collider;
    }

}
