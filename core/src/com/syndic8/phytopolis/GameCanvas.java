package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.syndic8.phytopolis.util.OSUtils;

import java.util.List;

public class GameCanvas {

    private final OrthographicCamera camera;
    private final OrthographicCamera gameCamera;
    private final OrthographicCamera hudCamera;
    private final OrthographicCamera textCamera;
    private final Viewport viewport;
    private final Viewport gameViewport;
    private final Viewport hudViewport;
    private final Viewport textViewport;
    private final Vector3 cameraCache;
    private final List<Graphics.DisplayMode> displayModes;
    private final FileHandle configFile;
    private final JsonValue settingsJson;
    private final int[] fps;
    private final float width;
    private final float height;
    private int windowHeight;
    private int windowWidth;
    private int currentFpsIndex;
    private Graphics.DisplayMode resolution;
    private boolean windowed;
    /**
     * Drawing context to handle textures AND POLYGONS as sprites
     */
    private PolygonSpriteBatch spriteBatch;
    /**
     * Drawing context to handle HUD
     */
    private SpriteBatch hudBatch;
    private ShapeRenderer shapeRenderer;
    /**
     * Track whether we are active (for error checking)
     */
    private DrawPass active;
    /**
     * The current color blending mode
     */
    private BlendState blend;
    /**
     * Affine cache for current sprite to draw
     */
    private Affine2 local;
    /**
     * Cache object to handle raw textures
     */
    private TextureRegion holder;

    /**
     * Creates a new GameCanvas determined by the application configuration.
     * <p>
     * Width, height, and fullscreen are taken from the LWGJApplicationConfig
     * object used to start the application.  This constructor initializes all
     * of the necessary graphics objects.
     */
    public GameCanvas(List<Graphics.DisplayMode> dm) {
        width = 16f;
        height = 9f;
        active = DrawPass.INACTIVE;
        spriteBatch = new PolygonSpriteBatch();
        hudBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        displayModes = dm;
        configFile = Gdx.files.absolute(OSUtils.getConfigFile());
        JsonReader settingsJsonReader = new JsonReader();
        settingsJson = settingsJsonReader.parse(configFile);
        int resolutionIndex = settingsJson.getInt("resolutionIndex");
        if (resolutionIndex == -1) resolutionIndex = displayModes.size() - 1;
        resolution = displayModes.get(resolutionIndex);
        windowWidth = settingsJson.getInt("windowWidth");
        windowHeight = settingsJson.getInt("windowHeight");
        windowed = settingsJson.getBoolean("windowed", false);
        fps = new int[]{0, 15, 30, 45, 60, 90, 120};
        currentFpsIndex = settingsJson.getInt("fpsIndex", 0);
        applyOptions();

        // Set the projection matrix (for proper scaling)
        camera = new OrthographicCamera(width, height);
        gameCamera = new OrthographicCamera(width, height);
        hudCamera = new OrthographicCamera(width, height);
        textCamera = new OrthographicCamera(width * 100f, height * 100f);

        viewport = new FitViewport(width, height, camera);
        gameViewport = new FitViewport(width, height, gameCamera);
        hudViewport = new FitViewport(width, height, hudCamera);
        textViewport = new FitViewport(width * 100f, height * 100f, textCamera);

        int screenWidth = Gdx.graphics.getDisplayMode().width;
        int screenHeight = Gdx.graphics.getDisplayMode().height;
        viewport.setScreenSize(screenWidth, screenHeight);
        gameViewport.setScreenSize(screenWidth, screenHeight);
        hudViewport.setScreenSize(screenWidth, screenHeight);
        textViewport.setScreenSize(screenWidth, screenHeight);
        camera.position.set(width / 2f, height / 2f, 0);
        gameCamera.position.set(width / 2f, height / 2f, 0);
        hudCamera.position.set(width / 2f, height / 2f, 0);
        textCamera.position.set(width * 100f / 2f, height * 100f / 2f, 0);

        camera.update();
        gameCamera.update();
        hudCamera.update();
        textCamera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        hudBatch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        // Initialize the cache objects
        holder = new TextureRegion();
        local = new Affine2();
        cameraCache = new Vector3();

        resizeCanvas();
    }

    public void applyOptions() {
        if (!windowed) {
            Gdx.graphics.setFullscreenMode(resolution);
        } else {
            Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
        }
        int currentFps = fps[currentFpsIndex];
        Gdx.graphics.setForegroundFPS(currentFps);
        Gdx.graphics.setVSync(currentFps == 0);
        saveOptions();
    }

    /**
     * Resets the SpriteBatch camera when this canvas is resized.
     * <p>
     * If you do not call this when the window is resized, you will get
     * weird scaling issues.
     */
    public void resizeCanvas() {
        spriteBatch.getProjectionMatrix()
                .setToOrtho2D(0, 0, getWidth(), getHeight());
        hudBatch.getProjectionMatrix()
                .setToOrtho2D(0, 0, getWidth(), getHeight());
        shapeRenderer.getProjectionMatrix()
                .setToOrtho2D(0, 0, getWidth(), getHeight());
        if (windowed) {
            windowWidth = Gdx.graphics.getWidth();
            windowHeight = Gdx.graphics.getHeight();
        }
        saveOptions();
    }

    public void saveOptions() {
        settingsJson.get("resolutionIndex")
                .set(displayModes.indexOf(resolution), null);
        settingsJson.get("fpsIndex").set(currentFpsIndex, null);
        settingsJson.get("windowed").set(windowed);
        if (windowed) {
            settingsJson.get("windowWidth").set(windowWidth, null);
            settingsJson.get("windowHeight").set(windowHeight, null);
        }
        configFile.writeString(settingsJson.prettyPrint(JsonWriter.OutputType.json,
                                                        0), false);
    }

    /**
     * Returns the width of this canvas
     * <p>
     * This currently gets its value from Gdx.graphics.getWidth()
     *
     * @return the width of this canvas
     */
    public float getWidth() {
        return width;
    }

    /**
     * Returns the height of this canvas
     * <p>
     * This currently gets its value from Gdx.graphics.getHeight()
     *
     * @return the height of this canvas
     */
    public float getHeight() {
        return height;
    }

    public void setWorldSize(float w) {
        float h = w * height / width;
        gameViewport.setWorldSize(w, h);
        gameCamera.position.set(w / 2f, h / 2f, 0);
        gameCamera.update();
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public void beginShape() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    public void endShape() {
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void cameraUpdate(Vector2 pos, boolean interpolate) {
        cameraCache.set(pos.x, pos.y, 0);
        if (interpolate) gameCamera.position.interpolate(cameraCache,
                                                         0.75f,
                                                         Interpolation.fade);
        else gameCamera.position.set(pos, 0);
        gameCamera.update();
    }

    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas",
                          "Cannot dispose while drawing active",
                          new IllegalStateException());
            return;
        }
        spriteBatch.dispose();
        hudBatch.dispose();
        shapeRenderer.dispose();
        hudBatch = null;
        spriteBatch = null;
        shapeRenderer = null;
        local = null;
        holder = null;
    }

    public void resizeScreen(int w, int h) {
        viewport.update(w, h);
        gameViewport.update(w, h);
        hudViewport.update(w, h);
        textViewport.update(w, h);
        resizeCanvas();
    }

    public Vector2 unprojectGame(Vector2 proj) {
        return gameViewport.unproject(proj);
    }

    public Vector2 unproject(Vector2 proj) {
        return viewport.unproject(proj);
    }

    public float getCameraY() {
        return camera.position.y;
    }

    /**
     * Clear the screen so that we can start a new animation frame
     */
    public void clear() {
        ScreenUtils.clear(Color.BLACK);
        camera.position.set(new Vector2(width / 2f, height / 2f), 0);
    }

    /**
     * Sets the color blending state for this canvas.
     * <p>
     * Any texture draw after this call will use the rules of this blend
     * state to composite with other textures.  Unlike the other setters, if it is
     * perfectly safe to use this setter while  drawing is active (e.g. in-between
     * a begin-end pair).
     *
     * @param state the color blending rule
     */
    public void setBlendState(BlendState state) {
        if (state == blend) {
            return;
        }
        switch (state) {
            case NO_PREMULT:
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,
                                             GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case ALPHA_BLEND:
                spriteBatch.setBlendFunction(GL20.GL_ONE,
                                             GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case ADDITIVE:
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                break;
            case OPAQUE:
                spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
                break;
        }
        blend = state;
    }

    public void beginHud() {
        hudViewport.apply();
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();
        active = DrawPass.STANDARD;
    }

    /**
     * Start a standard drawing sequence.
     * <p>
     * Nothing is flushed to the graphics card until the method end() is called.
     */
    public void begin() {
        viewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        active = DrawPass.STANDARD;
    }

    /**
     * Start a standard drawing sequence.
     * <p>
     * Nothing is flushed to the graphics card until the method end() is called.
     */
    public void beginGame() {
        gameViewport.apply();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        active = DrawPass.STANDARD;
    }

    /**
     * Ends a drawing sequence, flushing textures to the graphics card.
     */
    public void end() {
        spriteBatch.end();
        active = DrawPass.INACTIVE;
    }

    public void endHud() {
        hudBatch.end();
    }

    /**
     * Draws a TextureRegion t to the HUD batch at the given coordinates.
     *
     * @param t
     * @param x
     * @param y
     */
    public void drawHud(TextureRegion t, float x, float y, float w, float h) {
        hudBatch.draw(t, x, y, w, h);
    }

    /**
     * Draws a TextureRegion t to the HUD batch at the given coordinates.
     *
     * @param t
     * @param x
     * @param y
     */
    public void drawHud(Texture t, float x, float y, float w, float h) {
        hudBatch.draw(t, x, y, w, h);
    }

    /**
     * Draws the tinted texture at the given position.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image  The texture to draw
     * @param tint   The color tint
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(Texture image,
                     Color tint,
                     float x,
                     float y,
                     float width,
                     float height) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        spriteBatch.setColor(tint);
        spriteBatch.draw(image, x, y, width, height);
    }

    /**
     * Draws the tinted texture at the given position.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     * region
     *
     * @param region The texture to draw
     * @param tint   The color tint
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(TextureRegion region,
                     Color tint,
                     float x,
                     float y,
                     float width,
                     float height) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x, y, width, height);
    }

    /**
     * Draws the tinted texture with the given transformations
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     * <p>
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param ox    The x-coordinate of texture origin (in pixels)
     * @param oy    The y-coordinate of texture origin (in pixels)
     * @param x     The x-coordinate of the texture origin (on screen)
     * @param y     The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx    The x-axis scaling factor
     * @param sy    The y-axis scaling factor
     */
    public void draw(Texture image,
                     Color tint,
                     float ox,
                     float oy,
                     float x,
                     float y,
                     float angle,
                     float sx,
                     float sy) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        // Call the master drawing method (more efficient that base method)
        holder.setRegion(image);
        draw(holder, tint, ox, oy, x, y, angle, sx, sy);
    }

    /**
     * Draws the tinted texture region (filmstrip) with the given transformations
     * <p>
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     * <p>
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The texture to draw
     * @param tint   The color tint
     * @param ox     The x-coordinate of texture origin (in pixels)
     * @param oy     The y-coordinate of texture origin (in pixels)
     * @param x      The x-coordinate of the texture origin (on screen)
     * @param y      The y-coordinate of the texture origin (on screen)
     * @param angle  The rotation angle (in degrees) about the origin.
     * @param sx     The x-axis scaling factor
     * @param sy     The y-axis scaling factor
     */
    public void draw(TextureRegion region,
                     Color tint,
                     float ox,
                     float oy,
                     float x,
                     float y,
                     float angle,
                     float sx,
                     float sy) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        computeTransform(ox, oy, x, y, angle, sx, sy);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region,
                         region.getRegionWidth(),
                         region.getRegionHeight(),
                         local);
    }

    private void computeTransform(float ox,
                                  float oy,
                                  float x,
                                  float y,
                                  float angle,
                                  float sx,
                                  float sy) {
        local.setToTranslation(x, y);
        local.rotate(180.0f * angle / (float) Math.PI);
        local.scale(sx, sy);
        local.translate(-ox, -oy);
    }

    /**
     * Draws the polygonal region with the given transformations
     * <p>
     * A polygon region is a texture region with attached vertices so that it draws a
     * textured polygon. The polygon vertices are relative to the texture file.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     * <p>
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The polygon to draw
     * @param tint   The color tint
     * @param ox     The x-coordinate of texture origin (in pixels)
     * @param oy     The y-coordinate of texture origin (in pixels)
     * @param x      The x-coordinate of the texture origin (on screen)
     * @param y      The y-coordinate of the texture origin (on screen)
     * @param angle  The rotation angle (in degrees) about the origin.
     * @param sx     The x-axis scaling factor
     * @param sy     The y-axis scaling factor
     */
    public void draw(PolygonRegion region,
                     Color tint,
                     float ox,
                     float oy,
                     float x,
                     float y,
                     float angle,
                     float sx,
                     float sy) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        TextureRegion bounds = region.getRegion();
        spriteBatch.setColor(tint);
        spriteBatch.draw(region,
                         x,
                         y,
                         ox,
                         oy,
                         bounds.getRegionWidth(),
                         bounds.getRegionHeight(),
                         sx,
                         sy,
                         180.0f * angle / (float) Math.PI);
    }

    public Viewport getTextViewport() {
        return textViewport;
    }

    public void updateOption(GraphicsOption opn) {
        switch (opn) {
            case RESOLUTION:
                int resolutionIndex = (displayModes.indexOf(resolution) + 1) %
                        displayModes.size();
                resolution = displayModes.get(resolutionIndex);
                break;
            case WINDOWED:
                windowed = !windowed;
                break;
            case FPS:
                currentFpsIndex = (currentFpsIndex + 1) % fps.length;
                break;
        }
    }

    public String getOptionValueString(GraphicsOption opn) {
        switch (opn) {
            case RESOLUTION:
                return String.format("%dx%d @ %d",
                                     resolution.width,
                                     resolution.height,
                                     resolution.refreshRate);
            case WINDOWED:
                return (windowed ? "ON" : "OFF");
            case FPS:
                int currentFps = fps[currentFpsIndex];
                return (currentFps == 0 ? "VSYNC" : currentFps + " FPS");
        }
        return "Unknown";
    }

    /**
     * Enumeration to track which pass we are in
     */
    private enum DrawPass {
        /**
         * We are not drawing
         */
        INACTIVE,
        /**
         * We are drawing sprites
         */
        STANDARD
    }

    /**
     * Enumeration of supported BlendStates.
     * <p>
     * For reasons of convenience, we do not allow user-defined blend functions.
     * 99% of the time, we find that the following blend modes are sufficient
     * (particularly with 2D games).
     */
    public enum BlendState {
        /**
         * Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT)
         */
        ALPHA_BLEND,
        /**
         * Alpha blending on, assuming the colors have no pre-multipled alpha
         */
        NO_PREMULT,
        /**
         * Color values are added together, causing a white-out effect
         */
        ADDITIVE,
        /**
         * Color values are draw on top of one another with no transparency support
         */
        OPAQUE
    }

    public enum GraphicsOption {RESOLUTION, WINDOWED, FPS}

}
