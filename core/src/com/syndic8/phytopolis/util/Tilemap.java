package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.WorldController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Resource;
import com.syndic8.phytopolis.level.models.Sun;
import com.syndic8.phytopolis.level.models.Tile;
import com.syndic8.phytopolis.level.models.Water;

import java.util.ArrayList;
import java.util.List;

public class Tilemap {

    PooledList<Tile> tiles;
    AssetDirectory directory;
    JsonValue tilemap;
    float worldWidth;
    float worldHeight;
    int tilemapHeight;
    int tilemapWidth;
    float tileHeight;
    float tileWidth;
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
     * @param dir The main assets directory.
     */
    public void gatherAssets(AssetDirectory dir) {
        JsonValue physicsLayer = tilemap.get("layers").get(0);
        tilemapHeight = physicsLayer.getInt("height");
        tilemapWidth = physicsLayer.getInt("width");
        tileHeight = worldHeight / tilemapHeight;
        tileWidth = worldWidth / tilemapWidth;
        directory = dir;

        List<Texture> resourceTextureList = new ArrayList<>();
        Texture tx = directory.getEntry("gameplay:water_filmstrip",
                                        Texture.class);
        Texture tx2 = directory.getEntry("gameplay:sun_filmstrip",
                                         Texture.class);
        resourceTextureList.add(tx);
        resourceTextureList.add(tx2);
        resourceTextures = resourceTextureList.toArray(new Texture[0]);
    }

    /**
     * Populates the given WorldController with the tiles on the physics
     * layer and the resources on the resource layer of the tilemap.
     *
     * @param ctrl The WorldController to populate.
     */
    public void populateLevel(WorldController ctrl) {
        JsonValue physicsLayer = tilemap.get("layers").get(0);
        String tilesetName = tilemap.get("tilesets").get(0).getString("source");
        JsonValue tilesetJson = directory.getEntry(tilesetName,
                                                   JsonValue.class);
        float tilePixelWidth = tilesetJson.getFloat("tilewidth");
        float tilePixelHeight = tilesetJson.getFloat("tileheight");
        JsonValue tilesJson = tilesetJson.get("tiles");
        tiles = new PooledList<>();

        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                int tileValue = physicsLayer.get("data").asIntArray()[
                        row * tilemapWidth + col];
                if (tileValue != 0) {
                    float x0 = col * tileWidth;
                    float x1 = (col + 1) * tileWidth;
                    float y0 = worldHeight - (row + 1) * tileHeight;
                    float y1 = worldHeight - row * tileHeight;
                    JsonValue tileJson = tilesJson.get(tileValue - 1);
                    Texture tx = new Texture(
                            "gameplay/tiles/" + tileJson.getString("image"));
                    boolean hasCollider = tileJson.has("objectgroup");
                    boolean collideTop = tileJson.get("properties")
                            .get(0)
                            .getBoolean("value");
                    Tile tile = new Tile(this,
                                         new Vector2(x0, y0),
                                         collideTop,
                                         tx);
                    if (hasCollider) {
                        JsonValue colliderJson = tileJson.get("objectgroup")
                                .get("objects")
                                .get(0);
                        float cx0 = x0 +
                                colliderJson.getFloat("x") / tilePixelWidth *
                                        tileWidth;
                        float cy0 = y0 +
                                colliderJson.getFloat("y") / tilePixelHeight *
                                        tileHeight;
                        float cx1 = cx0 + colliderJson.getFloat("width") /
                                tilePixelWidth * tileWidth;
                        float cy1 = cy0 + colliderJson.getFloat("height") /
                                tilePixelHeight * tileHeight;
                        tile.addCollider(new float[]{cx0,
                                cy1,
                                cx1,
                                cy1,
                                cx1,
                                cy0,
                                cx0,
                                cy0});
                        ctrl.addObject(tile.getCollider());
                        tile.fixColliderUserData();
                    }
                    tiles.add(tile);
                }
            }
        }
        populateResources(ctrl);
    }

    private void populateResources(WorldController ctrl) {
        JsonValue resourceLayer = tilemap.get("layers").get(1);
        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                if (resourceLayer.get("data").asIntArray()[row * tilemapWidth +
                        col] != 0) {
                    float xMid = (col + 0.5f) * tileWidth;
                    float yMid = worldHeight - (row + 0.5f) * tileHeight;
                    FilmStrip waterFilmstrip = new FilmStrip(resourceTextures[0],
                                                             1,
                                                             13);
                    FilmStrip sunFilmstrip = new FilmStrip(resourceTextures[1],
                                                           1,
                                                           9);
                    Water w = new Water(xMid,
                                        yMid,
                                        tileWidth,
                                        tileHeight,
                                        waterFilmstrip,
                                        this,
                                        1);
                    Sun s = new Sun(xMid,
                                    yMid,
                                    tileWidth,
                                    tileHeight,
                                    sunFilmstrip,
                                    this,
                                    1);
                    ctrl.addObject(w);
                    ctrl.addObject(s);
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
        for (Tile tile : tiles) tile.draw(c);
    }

}
