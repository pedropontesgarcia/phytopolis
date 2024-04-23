package com.syndic8.phytopolis;

/*
 * MainMenuMode.java
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.ScreenListener;

/**
 * Class that provides a loading screen for the state of the game.
 * <p>
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 * <p>
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class MainMenuMode implements Screen {

    /**
     * Default budget for asset loader (do nothing but load 60 fps)
     */
    private static final int DEFAULT_BUDGET = 15;
    /**
     * Internal assets for this loading screen
     */
    private final AssetDirectory internal;
    /**
     * The actual assets to be loaded
     */
    private final AssetDirectory assets;
    /**
     * Background texture for start-up
     */
    private final Texture background;
    /**
     * Logo texture for start-up
     */
    private final Texture logo;
    /**
     * Reference to GameCanvas created by the root
     */
    private final GameCanvas canvas;
    private final Rectangle bounds;
    private float tmr;
    /**
     * The current state of the play button
     */
    private int pressState;
    /**
     * Play button to display when done
     */
    private Texture playButton;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;
    /**
     * Current progress (0 to 1) of the asset manager
     */
    private float progress;
    /**
     * The amount of time to devote to loading assets (as opposed to on screen hints, etc.)
     */
    private int budget;
    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    private Music backgroundMusic;

    /**
     * Creates a MainMenuMode with the default budget, size and position.
     *
     * @param file   The asset directory to load in the background
     * @param canvas The game canvas to draw to
     */
    public MainMenuMode(String file, GameCanvas canvas) {
        this(file, canvas, DEFAULT_BUDGET);
    }

    /**
     * Creates a MainMenuMode with the default size and position.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param file   The asset directory to load in the background
     * @param canvas The game canvas to draw to
     * @param millis The loading budget in milliseconds
     */
    public MainMenuMode(String file, GameCanvas canvas, int millis) {
        tmr = 0;
        this.canvas = canvas;
        budget = millis;
        bounds = new Rectangle(0, 0, 16, 9);

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(), canvas.getHeight());

        // We need these files loaded immediately
        internal = new AssetDirectory("mainmenu.json");
        internal.loadAssets();
        internal.finishLoading();

        // Load the next two images immediately.
        playButton = null;
        background = internal.getEntry("background", Texture.class);
        background.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        logo = internal.getEntry("logo", Texture.class);
        logo.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        // No progress so far.
        progress = 0;
        pressState = 0;

        // Start loading the real assets
        assets = new AssetDirectory(file);
        assets.loadAssets();
        active = true;
    }

    /**
     * Returns the budget for the asset loader.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 1;
    }

    /**
     * Returns the asset directory produced by this loading screen
     * <p>
     * This asset loader is NOT owned by this loading scene, so it persists even
     * after the scene is disposed.  It is your responsbility to unload the
     * assets in this directory.
     *
     * @return the asset directory produced by this loading screen
     */
    public AssetDirectory getAssets() {
        return assets;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }

    /**
     * Update the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        tmr += delta;
        InputController.getInstance().readInput(bounds, Vector2.Zero.add(1, 1));
        if (playButton == null) {
            assets.update(budget);
            this.progress = assets.getProgress();
            if (progress >= 1.0f) {
                this.progress = 1.0f;
                playButton = internal.getEntry("play", Texture.class);
                playButton.setFilter(TextureFilter.Linear,
                                     TextureFilter.Linear);
                if(backgroundMusic == null){
                    backgroundMusic = assets.getEntry("newgrowth", Music.class);
                    backgroundMusic.setLooping(true);
                    backgroundMusic.play();
                }
            }
        } else if (InputController.getInstance().didSecondary()) {
            pressState = 1;
        }
    }

    public Music getBackgroundMusic(){
        return backgroundMusic;
    }

    /**
     * Draw the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(background,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());

        canvas.draw(logo,
                    Color.WHITE,
                    logo.getWidth() / 2.0f,
                    logo.getHeight() / 2.0f,
                    canvas.getWidth() / 2.0f,
                    canvas.getHeight() * 2.0f / 3.0f,
                    0,
                    (float) canvas.getWidth() / logo.getWidth(),
                    (float) canvas.getHeight() / logo.getHeight());
        Color tint = (pressState == 1 ?
                Color.GRAY :
                new Color(1f, 1f, 1f, (float) Math.pow(Math.sin(tmr), 2)));
        if (progress == 1.0f) {
            canvas.draw(playButton,
                        tint,
                        playButton.getWidth() / 2.0f,
                        playButton.getHeight() / 2.0f,
                        canvas.getWidth() / 2.0f,
                        canvas.getHeight() / 4.0f,
                        0,
                        0.001f,
                        0.001f);
        }
        canvas.end();
    }

    /**
     * Called when the Screen should render itself.
     * <p>
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        float sx = ((float) width);
        float sy = ((float) height);
        scale = (Math.min(sx, sy));
        /**
         * The height of the canvas window (necessary since sprite origin != screen origin)
         */
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

}