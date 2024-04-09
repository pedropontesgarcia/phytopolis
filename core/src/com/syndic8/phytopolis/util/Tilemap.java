package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.WorldController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.PolygonObject;

import java.util.ArrayList;
import java.util.List;

public class Tilemap {

    float worldWidth;
    float worldHeight;
    JsonValue tilemap;
    Texture[] textures;

    public Tilemap(float w, float h, JsonValue tm) {
        worldWidth = w;
        worldHeight = h;
        tilemap = tm;
    }

    public void gatherAssets(AssetDirectory directory) {
        List<Texture> textureList = new ArrayList<>();
        String tilesetName = tilemap.get("tilesets").get(0).getString("source");
        JsonValue tileset = directory.getEntry(tilesetName, JsonValue.class);
        JsonValue tiles = tileset.get("tiles");
        for (int i = 0; i < tiles.size; i++) {
            JsonValue tile = tiles.get(i);
            Texture tx = directory.getEntry(tile.getString("image"),
                                            Texture.class);
            textureList.add(tx);
        }
        textures = textureList.toArray(new Texture[0]);
    }

    public void populateLevel(WorldController ctrl) {
        JsonValue physicsLayer = tilemap.get("layers").get(0);
        int tilemapHeight = physicsLayer.getInt("height");
        int tilemapWidth = physicsLayer.getInt("width");
        float tileHeight = worldHeight / tilemapHeight;
        float tileWidth = worldWidth / tilemapWidth;
        String wname = "wall";
        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                if (physicsLayer.get("data").asIntArray()[row * tilemapWidth +
                        col] == 1) {
                    PolygonObject obj;
                    float x0 = col * tileWidth;
                    float x1 = (col + 1) * tileWidth;
                    float y0 = worldHeight - (row + 1) * tileHeight;
                    float y1 = worldHeight - row * tileHeight;
                    obj = new PolygonObject(new float[]{x0,
                            y1,
                            x1,
                            y1,
                            x1,
                            y0,
                            x0,
                            y0}, 0, 0);
                    obj.setBodyType(BodyDef.BodyType.StaticBody);
                    obj.setDensity(0);
                    obj.setFriction(0);
                    obj.setRestitution(0);
                    obj.setName(wname + row + col);
                    ctrl.addObject(obj);
                }
            }
        }
    }

    public void draw(GameCanvas c) {
        JsonValue visualLayer = tilemap.get("layers").get(1);
        int tilemapHeight = visualLayer.getInt("height");
        int tilemapWidth = visualLayer.getInt("width");
        float tileHeight = worldHeight / tilemapHeight;
        float tileWidth = worldWidth / tilemapWidth;
        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                int tileValue = visualLayer.get("data").asIntArray()[
                        row * tilemapWidth + col];
                if (tileValue != 0) {
                    float x0 = col * tileWidth;
                    float x1 = (col + 1) * tileWidth;
                    float y0 = worldHeight - (row + 1) * tileHeight;
                    float y1 = worldHeight - row * tileHeight;
                    float scale = 120;
                    c.draw(textures[tileValue - 1],
                           Color.WHITE,
                           x0 * scale,
                           y0 * scale,
                           tileWidth * scale,
                           tileHeight * scale);
                }
            }
        }
    }

}
