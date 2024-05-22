package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.WorldController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Tile;
import com.syndic8.phytopolis.level.models.Water;

import java.util.ArrayList;
import java.util.List;

public class Tilemap {

    private static final String PHYSICS_TILESET = "tileset.tsx";
    private static final String RESOURCES_TILESET = "rsrc.tsx";
    private static final String HAZARDS_TILESET = "hazards.tsx";
    private final GameCanvas canvas;
    private final PooledList<Float> powerlineYVals;
    private final PooledList<Float> bugYVals;
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
    int time;
    int levelNumber;
    float fireRate;
    float victoryHeight;
    private Texture sunCircle;
    private Texture sunRay;
    private Texture sunSwirl;
    private String backgroundFile;
    private Texture victoryLine;

    /**
     * Constructs a tilemap from the world dimensions and a JSON file from
     * Tiled.
     *
     * @param tm the JSON tilemap file from Tiled.
     * @param c  the game canvas.
     */
    public Tilemap(JsonValue tm, GameCanvas c) {
        tilemap = tm;
        canvas = c;
        powerlineYVals = new PooledList<>();
        bugYVals = new PooledList<>();
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

    public float getTileWidth() {
        return tileWidth;
    }

    public int getTime() {
        return time;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getBackground() {
        return backgroundFile;
    }

    public float getFireRate() {
        return fireRate;
    }

    public float getVictoryHeight() {
        return victoryHeight;
    }

    /**
     * Gathers the assets from the tileset.
     *
     * @param dir The main assets directory.
     */
    public void gatherAssets(AssetDirectory dir) {
        JsonValue layersJson = tilemap.get("layers");
        JsonValue physicsLayer = null;
        for (JsonValue layerJson : layersJson) {
            if (layerJson.getString("name").equals("physics"))
                physicsLayer = layerJson;
        }
        assert physicsLayer != null;
        tilemapHeight = physicsLayer.getInt("height");
        tilemapWidth = physicsLayer.getInt("width");
        // I believe this 6f is correlated to the fact that
        // world coordinates and canvas size have a 1-to-1
        // correspondence when the tilemap is 6 tiles wide.
        // Our physics parameters are tuned to that, so changing
        // it will mess with the physics. It won't mess with the
        // scaling because that is taken care of automatically
        // by the viewport.
        worldWidth = canvas.getWidth() * tilemapWidth / 6f;
        // This 3f / 2f reflects the aspect ratio of our tiles,
        // which are 600px wide by 400px tall. In theory, any
        // tileset with that same aspect ratio should work.
        worldHeight = worldWidth / (tilemapWidth * 3f / 2f) *
                tilemap.getFloat("height");
        tileHeight = worldHeight / tilemapHeight;
        tileWidth = worldWidth / tilemapWidth;
        directory = dir;

        JsonValue propertiesJson = tilemap.get("properties");
        for (JsonValue propertyJson : propertiesJson) {
            if (propertyJson.getString("name").equals("firerate"))
                fireRate = propertyJson.getFloat("value");
            else if (propertyJson.getString("name").equals("time"))
                time = propertyJson.getInt("value");
            else if (propertyJson.getString("name").equals("victory"))
                victoryHeight = propertyJson.getFloat("value");
            else if (propertyJson.getString("name").equals("levelnumber"))
                levelNumber = propertyJson.getInt("value");
            else if (propertyJson.getString("name").equals("background"))
                backgroundFile = propertyJson.getString("value");
        }
        sunCircle = directory.getEntry("gameplay:sun_circle", Texture.class);
        sunSwirl = directory.getEntry("gameplay:sun_swirl", Texture.class);
        sunRay = directory.getEntry("gameplay:sun_ray", Texture.class);

        List<Texture> resourceTextureList = new ArrayList<>();
        Texture tx = directory.getEntry("gameplay:water_filmstrip",
                                        Texture.class);
        Texture tx2 = directory.getEntry("gameplay:sun_resource",
                                         Texture.class);
        victoryLine = directory.getEntry("gameplay:victoryline", Texture.class);
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
        populatePhysics(ctrl);
        populateResources(ctrl);
        populateHazards();
    }

    private void populatePhysics(WorldController ctrl) {
        JsonValue layersJson = tilemap.get("layers");
        JsonValue physicsLayer = null;
        for (JsonValue layerJson : layersJson) {
            if (layerJson.getString("name").equals("physics"))
                physicsLayer = layerJson;
        }
        assert physicsLayer != null;
        int i;
        for (i = 0; i < tilemap.get("tilesets").size; i++) {
            if (tilemap.get("tilesets")
                    .get(i)
                    .getString("source")
                    .equals(PHYSICS_TILESET)) break;
        }
        JsonValue tilesetJson = directory.getEntry(PHYSICS_TILESET,
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
                    JsonValue tileJson = null;
                    for (JsonValue indivTileJson : tilesJson) {
                        if (indivTileJson.getInt("id") == tileValue -
                                tilemap.get("tilesets")
                                        .get(i)
                                        .getInt("firstgid"))
                            tileJson = indivTileJson;
                    }
                    assert tileJson != null;
                    Texture tx = new Texture(
                            "gameplay/tiles/" + tileJson.getString("image"));
                    boolean hasCollider = tileJson.has("objectgroup");
                    boolean collideTop = tileJson.get("properties")
                            .get(0)
                            .getBoolean("value");
                    Tile tile = new Tile(getTilemapParams(),
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
                        float cy1 = y1 -
                                colliderJson.getFloat("y") / tilePixelHeight *
                                        tileHeight;
                        float cx1 = cx0 + colliderJson.getFloat("width") /
                                tilePixelWidth * tileWidth;
                        float cy0 = cy1 - colliderJson.getFloat("height") /
                                tilePixelHeight * tileHeight;
                        tile.addCollider(new float[]{cx0,
                                cy0,
                                cx0,
                                cy1,
                                cx1,
                                cy1,
                                cx1,
                                cy0});
                        ctrl.addObject(tile.getCollider());
                        tile.fixColliderUserData();
                    }
                    tiles.add(tile);
                }
            }
        }
    }

    private void populateResources(WorldController ctrl) {
        JsonValue layersJson = tilemap.get("layers");
        JsonValue resourceLayer = null;
        for (JsonValue layerJson : layersJson) {
            if (layerJson.getString("name").equals("resources"))
                resourceLayer = layerJson;
        }
        assert resourceLayer != null;
        int i;
        for (i = 0; i < tilemap.get("tilesets").size; i++) {
            if (tilemap.get("tilesets")
                    .get(i)
                    .getString("source")
                    .equals(RESOURCES_TILESET)) break;
        }
        JsonValue tilesetJson = directory.getEntry(RESOURCES_TILESET,
                                                   JsonValue.class);
        JsonValue tilesJson = tilesetJson.get("tiles");
        int numTimes = 0;
        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                int tileValue = resourceLayer.get("data").asIntArray()[
                        row * tilemapWidth + col];
                if (tileValue != 0) {
                    numTimes++;
                    JsonValue tileJson = tilesJson.get(tileValue - tilemap.get(
                            "tilesets").get(i).getInt("firstgid"));
                    float xMid = (col + 0.5f) * tileWidth;
                    float yMid = worldHeight - (row + 0.5f) * tileHeight;
                    if (tileJson.get("properties")
                            .get(0)
                            .getString("value")
                            .equals("water")) {
                        FilmStrip waterFilmstrip = new FilmStrip(
                                resourceTextures[0],
                                1,
                                26);
                        Water w = new Water(xMid,
                                            yMid,
                                            tileWidth,
                                            tileHeight,
                                            waterFilmstrip,
                                            this.getTilemapParams(),
                                            1);
                        ctrl.addObject(w);
                    }
                }
            }
        }
    }

    private void populateHazards() {
        JsonValue layersJson = tilemap.get("layers");
        JsonValue hazardsLayer = null;
        for (JsonValue layerJson : layersJson) {
            if (layerJson.getString("name").equals("hazards"))
                hazardsLayer = layerJson;
        }
        assert hazardsLayer != null;
        int i;
        for (i = 0; i < tilemap.get("tilesets").size; i++) {
            if (tilemap.get("tilesets")
                    .get(i)
                    .getString("source")
                    .equals(HAZARDS_TILESET)) break;
        }
        JsonValue tilesetJson = directory.getEntry(HAZARDS_TILESET,
                                                   JsonValue.class);
        JsonValue tilesJson = tilesetJson.get("tiles");

        for (int row = 0; row < tilemapHeight; row++) {
            for (int col = 0; col < tilemapWidth; col++) {
                int tileValue = hazardsLayer.get("data").asIntArray()[
                        row * tilemapWidth + col];
                if (tileValue != 0) {
                    float x0 = col * tileWidth;
                    float x1 = (col + 1) * tileWidth;
                    float y0 = worldHeight - (row + 1) * tileHeight;
                    float y1 = worldHeight - row * tileHeight;
                    JsonValue tileJson = tilesJson.get(tileValue - tilemap.get(
                            "tilesets").get(i).getInt("firstgid"));
                    Texture tx = new Texture(
                            "gameplay/tiles/" + tileJson.getString("image"));
                    Tile tile = new Tile(getTilemapParams(),
                                         new Vector2(x0, y0),
                                         false,
                                         tx);
                    if (tileJson.get("properties")
                            .get(0)
                            .getString("value")
                            .equals("powerline")) {
                        if (!powerlineYVals.contains(y0 + 0.5f * tileHeight))
                            powerlineYVals.add(y0 + 0.5f * tileHeight);
                    } else if (tileJson.get("properties")
                            .get(0)
                            .getString("value")
                            .equals("bug"))
                        if (!bugYVals.contains(y0 + 0.5f * tileHeight))
                            bugYVals.add(y0 + 0.5f * tileHeight);
                    tiles.add(tile);
                }
            }
        }
    }

    public TilemapParams getTilemapParams() {
        return new TilemapParams(tileWidth,
                                 tileHeight,
                                 tilemapWidth,
                                 tilemapHeight,
                                 worldWidth,
                                 worldHeight);
    }

    /**
     * Draws the visual layer of the tilemap to the canvas.
     *
     * @param c The game canvas.
     */
    public void draw(GameCanvas c) {
        for (Tile tile : tiles) tile.draw(c);
        float width = getWorldWidth();
        float height = getTileHeight();
        float x = 0;
        float y = victoryHeight * getTileHeight();
        c.draw(victoryLine, Color.WHITE, x, y, width, height);
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getTileHeight() {
        return tileHeight;
    }

    public void drawLevelOver(GameCanvas c) {
        for (Tile tile : tiles) tile.draw(c);
    }

    public PooledList<Float> getPowerlineYVals() {
        return powerlineYVals;
    }

    public PooledList<Float> getBugYVals() {
        return bugYVals;
    }

    public record TilemapParams(float tileWidth, float tileHeight,
                                float tilemapWidth, float tilemapHeight,
                                float worldWidth, float worldHeight) {

    }

}
