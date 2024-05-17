package com.syndic8.phytopolis;

/*
 * MainMenuMode.java
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.UIController;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.ScreenListener;
import com.syndic8.phytopolis.util.SharedAssetContainer;
import com.syndic8.phytopolis.util.menu.Menu;
import com.syndic8.phytopolis.util.menu.MenuContainer;
import com.syndic8.phytopolis.util.menu.MenuItem;
import com.syndic8.phytopolis.util.menu.OptionsMenu;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.SoundEffect;

/**
 * Class that provides a loading screen for the state of the game.
 * <p>
 * This class provides a basic template for a loading screen to be used at
 * the start of the game.
 */
public class MainMenuMode extends FadingScreen implements Screen {

    /**
     * Default budget for asset loader (do nothing but load 60 fps)
     */
    private static final int DEFAULT_BUDGET = 15;
    private static final float LOAD_DELAY = 0f;
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
    private final FilmStrip sprothFilmstrip;
    /**
     * Logo texture for start-up
     */
    private final Texture logo;
    /**
     * Reference to GameCanvas
     */
    private final GameCanvas canvas;
    private final SoundController soundController;
    private final Texture syndic8;
    Cursor cursor;
    private State state;
    private float timer;
    private boolean ready;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;
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
    private int backgroundMusic;
    private MenuContainer menuContainer;
    private Menu menu;
    private boolean exit;
    private int clickSound;
    private boolean menuWasMade;

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
     * @param c      The game canvas to draw to
     * @param millis The loading budget in milliseconds
     */
    public MainMenuMode(String file, GameCanvas c, int millis) {
        canvas = c;
        budget = millis;
        timer = 0;

        // We need these files loaded immediately
        internal = new AssetDirectory("mainmenu.json");
        internal.loadAssets();
        internal.finishLoading();

        // Load the next two images immediately.
        background = internal.getEntry("background", Texture.class);
        syndic8 = internal.getEntry("syndic8", Texture.class);
        Texture sprothTexture = internal.getEntry("sproth", Texture.class);
        sprothFilmstrip = new FilmStrip(sprothTexture, 1, 10);
        background.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        logo = internal.getEntry("logo", Texture.class);
        logo.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        // No progress so far.
        progress = 0;

        // Create menu
        //createMenu();
        this.menuWasMade = false;

        // Start loading the real assets
        assets = new AssetDirectory(file);
        assets.loadAssets();
        active = true;
        ready = false;
        exit = false;
        state = State.LOADING;

        // Load SoundController singleton
        soundController = SoundController.getInstance();
        this.backgroundMusic = -1;

        // Cursor
        TextureRegion cursorTexture = new TextureRegion(internal.getEntry(
                "cursor",
                Texture.class));
        Pixmap pixmap = UIController.getPixmapFromRegion(cursorTexture);
        cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        Gdx.graphics.setCursor(cursor);
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

    public int getBackgroundMusic() {
        return backgroundMusic;
    }

    public void setBackgroundMusic(int m) {
        backgroundMusic = m;
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        active = true;
        ready = false;
        exit = false;
        timer = 0;
        if (getFadeState() != Fade.SHOWN) {
            fadeIn(0.5f);
        }
        if (menuContainer != null) menuContainer.activate();
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

            if (ready && isFadeDone()) {
                listener.exitScreen(this, 0);
            }
        }
    }

    @Override
    public void resize(int i, int i1) {

    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
     * This is usually when it regains focus.
     */
    public void resume() {
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        active = false;
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
    public void update(float delta) {
        resetCursor();
        timer += delta;
        if (state == State.LOADING_FADEOUT && isFadeDone()) {
            state = State.MAIN_FADEIN;
            fadeIn(1);
            //playMusic();
        }
        if (state == State.MAIN_FADEIN && isFadeDone()) {
            state = State.MAIN;
        }
        if (progress < 1) {
            assets.update(budget);
            progress = assets.getProgress();
        }
        if (progress >= 1 && timer >= LOAD_DELAY && state == State.LOADING) {
            if (!menuWasMade) {
                createMenu();
                menuWasMade = true;
                menuContainer.activate();
            }
            if (backgroundMusic == -1) {
                AudioSource bgm = assets.getEntry("newgrowth",
                                                  AudioSource.class);
                backgroundMusic = soundController.addMusic(bgm);
                soundController.setMusic(backgroundMusic);
                soundController.setLooping(true);
                soundController.playMusic();
            }
            SoundEffect clickSoundEffect = assets.getEntry("click",
                                                           SoundEffect.class);
            clickSound = soundController.addSoundEffect(clickSoundEffect);
            SharedAssetContainer.getInstance().addSound("click", clickSound);
            progress = 1;
            state = State.LOADING_FADEOUT;
            fadeOut(0.5f);
        }
        if (state == State.LOADING || state == State.LOADING_FADEOUT) {
            sprothFilmstrip.setFrame(
                    (int) (timer / 0.1f) % sprothFilmstrip.getSize());
        }
        if (exit) {
            exit = false;
            ready = true;
            fadeOut(0.5f);
            doVolumeFade(false);
            menuContainer.deactivate();
        }
        if (menuContainer != null) menuContainer.update(delta);
        super.update(delta);
    }

    /**
     * Draw the status of this mode.
     */
    private void draw() {
        canvas.clear();
        if (state == State.MAIN_FADEIN || state == State.MAIN) {
            if (menuContainer.getMenu() == menu) {
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
                            canvas.getWidth() / logo.getWidth(),
                            canvas.getHeight() / logo.getHeight());
                canvas.end();
            }
            canvas.begin();
            menuContainer.draw(canvas);
            canvas.end();
        }
        if (state == State.LOADING || state == State.LOADING_FADEOUT) {
            canvas.begin();
            canvas.draw(syndic8,
                        Color.WHITE,
                        0,
                        0,
                        canvas.getWidth(),
                        canvas.getHeight());
            canvas.draw(sprothFilmstrip, Color.WHITE, 7, 4, 2, 2);
            canvas.end();
        }
        super.draw(canvas);
    }

    public void resetCursor() {
        Gdx.graphics.setCursor(cursor);
    }

    private void createMenu() {
        menu = new Menu(3, 0.1f, 0, -0.2f, 1, Align.center, Menu.DEFAULT_WIDTH);
        menuContainer = new MenuContainer(menu, canvas);
        Menu optionsMenu = new OptionsMenu(canvas, menuContainer, menu);
        ClickListener lvlListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (progress >= 1) SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                exit = true;
            }
        };
        ClickListener exitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (progress >= 1) SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                Gdx.app.exit();
            }
        };
        menu.addItem(new MenuItem("PLAY",
                                  0,
                                  lvlListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("OPTIONS",
                                  1,
                                  optionsMenu,
                                  menu,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("QUIT",
                                  2,
                                  exitListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menuContainer.populate();
    }
/*
    private void playMusic() {
        if (backgroundMusic == null) {
            backgroundMusic = assets.getEntry("newgrowth", AudioSource.class);
            int i = soundController.addMusic(backgroundMusic);
            soundController.setMusic(i);
            soundController.setLooping(true);
            soundController.playMusic();
        }
    }
    */

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener l) {
        listener = l;
    }

    public enum State {LOADING, LOADING_FADEOUT, MAIN_FADEIN, MAIN}

}