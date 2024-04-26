package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.levelselect.LevelBox;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.ScreenListener;
import com.syndic8.phytopolis.util.menu.Menu;
import com.syndic8.phytopolis.util.menu.MenuContainer;
import com.syndic8.phytopolis.util.menu.MenuItem;
import edu.cornell.gdiac.audio.AudioEngine;

public class LevelSelectMode extends FadingScreen implements Screen {

    private final Rectangle bounds;
    private final int numLevels = 6;
    private final LevelBox[] levelBoxes;
    private final Vector2 projMousePosCache;
    /**
     * Reference to GameCanvas created by the root
     */
    private final GameCanvas canvas;
    /**
     * Whether or not this screen is active
     */
    private boolean active;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;
    private FilmStrip background;
    private Texture lighting;
    private boolean ready;
    private Music backgroundMusic;
    private AudioEngine audioEngine;
    private Texture rs;
    private String level;
    private ExitCode exitCode;
    private MenuContainer menuContainer;

    public LevelSelectMode(GameCanvas c) {
        canvas = c;
        ready = false;
        bounds = new Rectangle(0, 0, 16, 9);

        //Setup levelboxes
        levelBoxes = new LevelBox[numLevels];
        projMousePosCache = new Vector2();
        createMenu();
    }

    private void createMenu() {
        Menu menu = new Menu(1, 0, 0.4f, -0.4f, Align.left);
        menuContainer = new MenuContainer(menu, canvas);
        ClickListener mainMenuListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ready = true;
                exitCode = ExitCode.EXIT_MAIN_MENU;
                fadeOut(0.5f);
            }
        };
        menu.addItem(new MenuItem("BACK",
                                  menu.getSeparation(),
                                  0,
                                  menu.getLength(),
                                  mainMenuListener,
                                  menuContainer,
                                  canvas,
                                  menu.getXOffset(),
                                  menu.getYOffset(),
                                  menu.getAlignment()));
        menuContainer.populate();
    }

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public void setBackgroundMusic(Music m) {
        backgroundMusic = m;
    }

    public void gatherAssets(AssetDirectory directory) {
        background = new FilmStrip(directory.getEntry("lvlsel:background",
                                                      Texture.class), 1, 4);
        lighting = directory.getEntry("lvlsel:lighting", Texture.class);
        rs = directory.getEntry("lvlsel:redsquare", Texture.class);
        fadeIn(0.5f);
        ready = false;
        //        backgroundMusic = directory.getEntry("newgrowth", Music.class);
        //        backgroundMusic.setLooping(true);
        //        backgroundMusic.play();
    }

    @Override
    public void show() {
        active = true;
        exitCode = null;
        ready = false;
        menuContainer.activate();
        // Set up levelboxes
        levelBoxes[0] = new LevelBox(canvas.getWidth() / 5f,
                                     canvas.getHeight() / 3.3f);
        levelBoxes[1] = new LevelBox(canvas.getWidth() / 2.3f,
                                     canvas.getHeight() / 3.3f);
        levelBoxes[2] = new LevelBox(canvas.getWidth() / 1.47f,
                                     canvas.getHeight() / 3.3f);
        for (LevelBox lb : levelBoxes) if (lb != null) lb.setTexture(rs);
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(1);
            backgroundMusic.play();
        }
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (ready && isFadeDone()) {
                listener.exitScreen(this, exitCode.ordinal());
            }
        }
    }

    public void update(float delta) {
        super.update(delta);
        menuContainer.update(delta);
        InputController ic = InputController.getInstance();
        projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unproject(projMousePosCache);
        float mouseX = unprojMousePos.x;
        float mouseY = unprojMousePos.y;
        for (LevelBox lb : levelBoxes) {
            if (lb != null) lb.setSelected(lb.inBounds(mouseX, mouseY));
        }
        InputController.getInstance().readInput(bounds, Vector2.Zero.add(1, 1));
        if (getSelectedPot() != -1 &&
                InputController.getInstance().didMousePress()) {
            setLevel();
            fadeOut(1);
            ready = true;
            exitCode = ExitCode.EXIT_LEVEL;
        }
        if (ready && exitCode == ExitCode.EXIT_LEVEL)
            backgroundMusic.setVolume(super.getVolume());
    }

    public void draw() {
        canvas.clear();
        canvas.begin();
        canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);
        background.setFrame(getSelectedPot() + 1);
        canvas.draw(background,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
        canvas.draw(lighting,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
        canvas.setBlendState(GameCanvas.BlendState.NO_PREMULT);
        for (LevelBox lb : levelBoxes) if (lb != null) lb.draw(canvas);
        menuContainer.draw(canvas);
        canvas.end();
        super.draw(canvas);
    }

    public int getSelectedPot() {
        for (int i = 0; i < numLevels; i++) {
            if (levelBoxes[i] != null && levelBoxes[i].getSelected()) return i;
        }
        return -1;
    }

    public void setLevel() {
        switch (getSelectedPot()) {
            case 0:
                level = "gameplay:lvl1";
                break;
            case 1:
                level = "gameplay:lvl2";
                break;
            case 2:
                level = "gameplay:lvl3";
                break;
        }
    }

    @Override
    public void resize(int i, int i1) {
        //Autogenerated, can ignore
    }

    @Override
    public void pause() {
        //Autogenerated, can ignore
    }

    @Override
    public void resume() {
        //Autogenerated, can ignore
    }

    @Override
    public void hide() {
        active = false;
        menuContainer.deactivate();
        if (exitCode == ExitCode.EXIT_LEVEL) backgroundMusic.stop();
    }

    @Override
    public void dispose() {
        backgroundMusic.dispose();
    }

    public void reset() {
        ready = false;
        fadeIn(0.5f);
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public String getLevel() {
        return level;
    }

    public enum ExitCode {EXIT_MAIN_MENU, EXIT_LEVEL}

}
