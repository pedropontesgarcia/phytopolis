package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.WorldController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.PolygonObject;
import com.syndic8.phytopolis.level.models.Resource;
import com.syndic8.phytopolis.level.models.Water;

import java.util.ArrayList;
import java.util.List;

public class Tilemap {

    JsonValue tilemap;
    float worldWidth;
    float worldHeight;
    int tilemapHeight;
    int tilemapWidth;
    float tileHeight;
    float tileWidth;
    Texture[] tileTextures;
    Texture[] resourceTextures;
    Resource[] resources;

    /**
     * Constructs a tilemap from the world dimensions and a JSON file from
     * Tiled.
     *
     * @param w  The world width, in world units.
     * @param h  The world height, in world units.
     * @param tm The JSON tilemap file from Tiled.
     */
    public Tilemap(float w, float h, JsonValue tm) {
        worldWidth = w;
        worldHeight = h;
        tilemap = tm;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }

    public int getTilemapHeight() {
        return tilemapHeight;
    }

    public int getTilemapWidth() {
        return tilemapWidth;
    }

    public float getTileHeight() {
        return tileHeight;
    }

    public float getTileWidth() {
        return tileWidth;
    }

    /**
     * Gathers the assets from the tileset.
     *
     * @param directory The main assets directory.
     */
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
        tileTextures = textureList.toArray(new Texture[0]);

        List<Texture> resourceTextureList = new ArrayList<>();
        Texture tx = directory.getEntry("gameplay:water_filmstrip",
                                        Texture.class);
        resourceTextureList.add(tx);
        resourceTextures = resourceTextureList.toArray(new Texture[0]);

        JsonValue physicsLayer = tilemap.get("layers").get(0);
        tilemapHeight = physicsLayer.getInt("height");
        tilemapWidth = physicsLayer.getInt("width");
        tileHeight = worldHeight / tilemapHeight;
        tileWidth = worldWidth / tilemapWidth;
    }

    /**
     * Populates the given WorldController with the tiles on the physics
     * layer and the resources on the resource layer of the tilemap.
     *
     * @param ctrl The WorldController to populate.
     */
    public void populateLevel(WorldController ctrl) {
        populateGeometry(ctrl);
        populateResources(ctrl);
    }

    public void populateGeometry(WorldController ctrl) {
        JsonValue physicsLayer = tilemap.get("layers").get(0);
        String wname = "wall";
        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                if (physicsLayer.get("data").asIntArray()[row * tilemapWidth +
                        col] != 0) {
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
                            y0}, 0, 0, this, 1);
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

    private void populateResources(WorldController ctrl) {
        JsonValue resourceLayer = tilemap.get("layers").get(2);
        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                if (resourceLayer.get("data").asIntArray()[row * tilemapWidth +
                        col] != 0) {
                    float xMid = (col + 0.5f) * tileWidth;
                    float yMid = worldHeight - (row + 0.5f) * tileHeight;
                    FilmStrip waterFilmstrip = new FilmStrip(resourceTextures[0],
                                                             1,
                                                             13);
                    Water w = new Water(xMid,
                                        yMid,
                                        tileWidth,
                                        tileHeight,
                                        waterFilmstrip,
                                        this,
                                        1);
                    ctrl.addObject(w);
                }
            }
        }
    }

    /**
     * Draws the visual layer of the tilemap to the canvas.
     *
     * @param c The game canvas.
     */
    public void draw(GameCanvas c) {
        JsonValue visualLayer = tilemap.get("layers").get(1);
        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                int tileValue = visualLayer.get("data").asIntArray()[
                        row * tilemapWidth + col];
                if (tileValue != 0) {
                    float x0 = col * tileWidth;
                    float x1 = (col + 1) * tileWidth;
                    float y0 = worldHeight - (row + 1) * tileHeight;
                    float y1 = worldHeight - row * tileHeight;
                    c.draw(tileTextures[tileValue - 1],
                           Color.WHITE,
                           x0,
                           y0,
                           tileWidth,
                           tileHeight);
                }
            }
        }
    }

}
