package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

public class GameCanvas {

    /**
     * Rendering context for the debug outlines
     */
    private final ShapeRenderer debugRender;
    /**
     * Camera for the underlying SpriteBatch
     */
    private final OrthographicCamera camera;
    /**
     * Camera for the underlying SpriteBatch
     */
    private final OrthographicCamera hudCamera;

    private final OrthographicCamera textCamera;
    private final Viewport viewport;
    private final Viewport hudViewport;
    private final Viewport textViewport;
    private final Vector3 cameraCache;
    private final Vector2 sizeCache;
    private final List<Graphics.DisplayMode> displayModes;
    /**
     * Value to cache window width (if we are currently full screen)
     */
    int width;
    /**
     * Value to cache window height (if we are currently full screen)
     */
    int height;
    private boolean vsync;
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
     * Affine cache for all sprites this drawing pass
     */
    private Matrix4 global;
    // CACHE OBJECTS
    private Vector2 vertex;
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
        width = 16;
        height = 9;
        active = DrawPass.INACTIVE;
        spriteBatch = new PolygonSpriteBatch();
        hudBatch = new SpriteBatch();
        debugRender = new ShapeRenderer();
        shapeRenderer = new ShapeRenderer();

        displayModes = dm;
        resolution = Gdx.graphics.getDisplayMode();
        windowed = !Gdx.graphics.isFullscreen();
        vsync = true;

        // Set the projection matrix (for proper scaling)
        camera = new OrthographicCamera(width, height);
        hudCamera = new OrthographicCamera(width, height);
        textCamera = new OrthographicCamera(width * 100f, height * 100f);

        viewport = new FitViewport(width, height, camera);
        hudViewport = new FitViewport(width, height, hudCamera);
        textViewport = new FitViewport(width * 100f, height * 100f, textCamera);

        int screenWidth = Gdx.graphics.getDisplayMode().width;
        int screenHeight = Gdx.graphics.getDisplayMode().height;
        viewport.setScreenSize(screenWidth, screenHeight);
        hudViewport.setScreenSize(screenWidth, screenHeight);
        textViewport.setScreenSize(screenWidth, screenHeight);
        camera.position.set(width / 2f, height / 2f, 0);
        hudCamera.position.set(width / 2f, height / 2f, 0);
        textCamera.position.set(width * 100f / 2f, height * 100f / 2f, 0);

        camera.update();
        hudCamera.update();
        textCamera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        hudBatch.setProjectionMatrix(hudCamera.combined);
        debugRender.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        // Initialize the cache objects
        holder = new TextureRegion();
        local = new Affine2();
        global = new Matrix4();
        vertex = new Vector2();
        cameraCache = new Vector3();
        sizeCache = new Vector2();
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

    public Vector2 unproject(Vector2 proj) {
        return viewport.unproject(proj);
    }

    public void cameraUpdate(Vector2 pos) {
        cameraCache.set(pos.x, pos.y, 0);
        camera.position.interpolate(cameraCache, 0.75f, Interpolation.fade);
        spriteBatch.setProjectionMatrix(camera.combined);
        camera.update();

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
        global = null;
        vertex = null;
        holder = null;
    }

    /**
     * Returns the dimensions of this canvas
     *
     * @return the dimensions of this canvas
     */
    public Vector2 getSize() {
        sizeCache.set(width, height);
        return sizeCache;
    }

    /**
     * Changes the width and height of this canvas
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param width  the canvas width
     * @param height the canvas height
     */
    public void setSize(int width, int height) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas",
                          "Cannot alter property while drawing active",
                          new IllegalStateException());
            return;
        }
        this.width = width;
        this.height = height;
        resizeCanvas();
    }

    /**
     * Resets the SpriteBatch camera when this canvas is resized.
     * <p>
     * If you do not call this when the window is resized, you will get
     * weird scaling issues.
     */
    public void resizeCanvas() {
        // Resizing screws up the spriteBatch projection matrix
        spriteBatch.getProjectionMatrix()
                .setToOrtho2D(0, 0, getWidth(), getHeight());
    }

    /**
     * Returns the width of this canvas
     * <p>
     * This currently gets its value from Gdx.graphics.getWidth()
     *
     * @return the width of this canvas
     */
    public int getWidth() {
        return width;
    }

    /**
     * Changes the width of this canvas
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param width the canvas width
     */
    public void setWidth(int width) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas",
                          "Cannot alter property while drawing active",
                          new IllegalStateException());
            return;
        }
        this.width = width;
        resizeCanvas();
    }

    /**
     * Returns the height of this canvas
     * <p>
     * This currently gets its value from Gdx.graphics.getHeight()
     *
     * @return the height of this canvas
     */
    public int getHeight() {
        return height;
    }

    /**
     * Changes the height of this canvas
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param height the canvas height
     */
    public void setHeight(int height) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas",
                          "Cannot alter property while drawing active",
                          new IllegalStateException());
            return;
        }
        this.height = height;
        resizeCanvas();
    }

    /**
     * Returns whether this canvas is currently fullscreen.
     *
     * @return whether this canvas is currently fullscreen.
     */
    public boolean isFullscreen() {
        return Gdx.graphics.isFullscreen();
    }

    /**
     * Sets whether or not this canvas should change to fullscreen.
     * <p>
     * If desktop is true, it will use the current desktop resolution for
     * fullscreen, and not the width and height set in the configuration
     * object at the start of the application. This parameter has no effect
     * if fullscreen is false.
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param value   Whether this canvas should change to
     *                fullscreen.
     * @param desktop Whether to use the current desktop resolution
     */
    public void setFullscreen(boolean value, boolean desktop) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas",
                          "Cannot alter property while drawing active",
                          new IllegalStateException());
            return;
        }
        if (value) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(width, height);
        }
    }

    public void resizeScreen(int width, int height) {
        viewport.update(width, height);
        hudViewport.update(width, height);
        textViewport.update(width, height);
    }

    public float getCameraY() {
        return camera.position.y;
    }

    public float getViewPortY() {
        return camera.viewportHeight;
    }

    /**
     * Clear the screen so that we can start a new animation frame
     */
    public void clear() {
        ScreenUtils.clear(Color.BLACK);
        camera.position.set(new Vector2(8f, 4.5f), 0);
    }

    /**
     * Start a standard drawing sequence.
     * <p>
     * Nothing is flushed to the graphics card until the method end() is called.
     *
     * @param affine the global transform apply to the camera
     */
    public void begin(Affine2 affine) {
        global.setAsAffine(affine);
        global.mulLeft(camera.combined);
        spriteBatch.setProjectionMatrix(global);

        setBlendState(BlendState.NO_PREMULT);
        spriteBatch.begin();
        active = DrawPass.STANDARD;
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
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();
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
     * Draws the tinted texture at the given position.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param x     The x-coordinate of the bottom left corner
     * @param y     The y-coordinate of the bottom left corner
     */
    public void draw(Texture image, float x, float y) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.draw(image, x, y);
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
     * Draws the tinted texture at the given position.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param x     The x-coordinate of the bottom left corner
     * @param y     The y-coordinate of the bottom left corner
     */
    public void draw(Texture image, Color tint, float x, float y) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        spriteBatch.setColor(tint);
        spriteBatch.draw(image, x, y);
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
     *
     * @param image  The texture to draw
     * @param tint   The color tint
     * @param ox     The x-coordinate of texture origin (in pixels)
     * @param oy     The y-coordinate of texture origin (in pixels)
     * @param x      The x-coordinate of the texture origin (on screen)
     * @param y      The y-coordinate of the texture origin (on screen)
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(Texture image,
                     Color tint,
                     float ox,
                     float oy,
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

        // Call the master drawing method (more efficient that base method)
        holder.setRegion(image);
        draw(holder, tint, x - ox, y - oy, width, height);
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
     * @param image     The texture to draw
     * @param tint      The color tint
     * @param ox        The x-coordinate of texture origin (in pixels)
     * @param oy        The y-coordinate of texture origin (in pixels)
     * @param transform The image transform
     */
    public void draw(Texture image,
                     Color tint,
                     float ox,
                     float oy,
                     Affine2 transform) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        // Call the master drawing method (we have to for transforms)
        holder.setRegion(image);
        draw(holder, tint, ox, oy, transform);
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
     * @param region The region to draw
     * @param tint   The color tint
     * @param ox     The x-coordinate of texture origin (in pixels)
     * @param oy     The y-coordinate of texture origin (in pixels)
     * @param affine The image transform
     */
    public void draw(TextureRegion region,
                     Color tint,
                     float ox,
                     float oy,
                     Affine2 affine) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        local.set(affine);
        local.translate(-ox, -oy);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region,
                         region.getRegionWidth(),
                         region.getRegionHeight(),
                         local);
    }

    /**
     * Draws the tinted texture region (filmstrip) at the given position.
     * <p>
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param region The texture to draw
     * @param tint   The color tint
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     */
    public void draw(TextureRegion region, Color tint, float x, float y) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x, y);
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
     * @param region The texture to draw
     * @param tint   The color tint
     * @param ox     The x-coordinate of texture origin (in pixels)
     * @param oy     The y-coordinate of texture origin (in pixels)
     * @param x      The x-coordinate of the texture origin (on screen)
     * @param y      The y-coordinate of the texture origin (on screen)
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(TextureRegion region,
                     Color tint,
                     float ox,
                     float oy,
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
        spriteBatch.draw(region, x - ox, y - oy, width, height);
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
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     */
    public void draw(PolygonRegion region, Color tint, float x, float y) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x, y);
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
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(PolygonRegion region,
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
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(PolygonRegion region,
                     Color tint,
                     float ox,
                     float oy,
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

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x - ox, y - oy, width, height);
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
     * @param affine The image transform
     */
    public void draw(PolygonRegion region,
                     Color tint,
                     float ox,
                     float oy,
                     Affine2 affine) {
        if (active != DrawPass.STANDARD) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active begin()",
                          new IllegalStateException());
            return;
        }

        local.set(affine);
        local.translate(-ox, -oy);
        computeVertices(local, region.getVertices());

        spriteBatch.setColor(tint);
        spriteBatch.draw(region, 0, 0);

        // Invert and restore
        local.inv();
        computeVertices(local, region.getVertices());
    }

    /**
     * Transform the given vertices by the affine transform
     */
    private void computeVertices(Affine2 affine, float[] vertices) {
        for (int ii = 0; ii < vertices.length; ii += 2) {
            vertex.set(vertices[2 * ii], vertices[2 * ii + 1]);
            affine.applyTo(vertex);
            vertices[2 * ii] = vertex.x;
            vertices[2 * ii + 1] = vertex.y;
        }
    }

    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x     The x-coordinate of the shape position
     * @param y     The y-coordinate of the shape position
     * @param angle The shape angle of rotation
     * @param sx    The amount to scale the x-axis
     * @param sy    The amount to scale the y-axis
     */
    public void drawPhysics(PolygonShape shape,
                            Color color,
                            float x,
                            float y,
                            float angle,
                            float sx,
                            float sy) {
        if (active != DrawPass.DEBUG) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active beginDebug()",
                          new IllegalStateException());
            return;
        }

        local.setToScaling(sx, sy);
        local.translate(x, y);
        local.rotateRad(angle);

        float x0, y0, x1, y1;
        debugRender.setColor(color);
        for (int ii = 0; ii < shape.getVertexCount() - 1; ii++) {
            shape.getVertex(ii, vertex);
            local.applyTo(vertex);
            x0 = vertex.x;
            y0 = vertex.y;
            shape.getVertex(ii + 1, vertex);
            local.applyTo(vertex);
            x1 = vertex.x;
            y1 = vertex.y;
            debugRender.line(x0, y0, x1, y1);
        }
        // Close the loop
        shape.getVertex(shape.getVertexCount() - 1, vertex);
        local.applyTo(vertex);
        x0 = vertex.x;
        y0 = vertex.y;
        shape.getVertex(0, vertex);
        local.applyTo(vertex);
        x1 = vertex.x;
        y1 = vertex.y;
        debugRender.line(x0, y0, x1, y1);
    }

    /**
     * Draws the outline of the given shape in the specified color
     * <p>
     * The position of the circle is ignored.  Only the radius is used. To move the
     * circle, change the x and y parameters.
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x     The x-coordinate of the shape position
     * @param y     The y-coordinate of the shape position
     * @param sx    The amount to scale the x-axis
     * @param sx    The amount to scale the y-axis
     */
    public void drawPhysics(CircleShape shape,
                            Color color,
                            float x,
                            float y,
                            float sx,
                            float sy) {
        if (active != DrawPass.DEBUG) {
            Gdx.app.error("GameCanvas",
                          "Cannot draw without active beginDebug()",
                          new IllegalStateException());
            return;
        }

        float x0 = x * sx;
        float y0 = y * sy;
        float w = shape.getRadius() * sx;
        float h = shape.getRadius() * sy;
        debugRender.setColor(color);
        debugRender.ellipse(x0 - w, y0 - h, 2 * w, 2 * h, 12);
    }

    public Viewport getTextViewport() {
        return textViewport;
    }

    public void updateOption(GraphicsOption opn) {
        switch (opn) {
            case RESOLUTION:
                resolution = displayModes.get(
                        (displayModes.indexOf(resolution) + 1) %
                                displayModes.size());
                if (!windowed) Gdx.graphics.setFullscreenMode(resolution);
                return;
            case WINDOWED:
                windowed = !windowed;
                if (!windowed) {
                    Gdx.graphics.setFullscreenMode(resolution);
                } else {
                    Gdx.graphics.setWindowedMode(1280, 720);
                    Gdx.graphics.setResizable(false);
                }
                return;
            case VSYNC:
                vsync = !vsync;
                Gdx.graphics.setVSync(vsync);
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
            case VSYNC:
                return (vsync ? "ON" : "OFF");
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
        STANDARD,
        /**
         * We are drawing outlines
         */
        DEBUG
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

    public enum GraphicsOption {RESOLUTION, WINDOWED, VSYNC}

}
