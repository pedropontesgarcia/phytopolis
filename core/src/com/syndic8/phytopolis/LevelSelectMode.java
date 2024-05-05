package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import edu.cornell.gdiac.audio.AudioSource;

import static com.syndic8.phytopolis.GDXRoot.ExitCode;

public class LevelSelectMode extends FadingScreen implements Screen {

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
    private boolean gathered;
    private int backgroundMusic;
    private AudioEngine audioEngine;
    private Texture rs;
    private String level;
    private ExitCode exitCode;
    private MenuContainer menuContainer;
    private SoundController soundController;

    public LevelSelectMode(GameCanvas c) {
        canvas = c;
        ready = false;

        //Setup levelboxes
        levelBoxes = new LevelBox[numLevels];
        projMousePosCache = new Vector2();
        createMenu();
        gathered = false;
        this.soundController = SoundController.getInstance();
    }

    private void createMenu() {
        Menu menu = new Menu(1,
                             0,
                             0.4f,
                             -0.4f,
                             1,
                             Align.center,
                             Menu.DEFAULT_WIDTH);
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
                                  0,
                                  mainMenuListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menuContainer.populate();
    }

    public int getBackgroundMusic() {
        return backgroundMusic;
    }

    public void setBackgroundMusic(int i) {
        backgroundMusic = i;
        if(i != soundController.getMusicQueuePos()) soundController.setMusic(i);
    }

    public void gatherAssets(AssetDirectory directory) {
        if (!gathered) {
            background = new FilmStrip(directory.getEntry("lvlsel:background",
                                                          Texture.class), 1, 4);
            lighting = directory.getEntry("lvlsel:lighting", Texture.class);
            rs = directory.getEntry("lvlsel:redsquare", Texture.class);
            gathered = true;
        }
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
//        if (backgroundMusic != null) {
//            backgroundMusic.setVolume(1);
//            backgroundMusic.play();
//        }
        if (backgroundMusic != soundController.getMusicQueuePos()) {
            soundController.setMusic(backgroundMusic);
            soundController.setLooping(true);
            soundController.playMusic();
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
        InputController ic = InputController.getInstance();
        ic.readInput();
        super.update(delta);
        menuContainer.update(delta);
        projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unproject(projMousePosCache);
        float mouseX = unprojMousePos.x;
        float mouseY = unprojMousePos.y;
        for (LevelBox lb : levelBoxes) {
            if (lb != null) lb.setSelected(lb.inBounds(mouseX, mouseY));
        }
        if (getSelectedPot() != -1 &&
                InputController.getInstance().didMousePress() && !ready) {
            setLevel();
            fadeOut(1);
            ready = true;
            exitCode = ExitCode.EXIT_LEVELS;
        }
        if (ready && exitCode == ExitCode.EXIT_LEVELS)
            soundController.setMusicVolume(super.getVolume());
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
        //if (exitCode == ExitCode.EXIT_LEVELS) soundController.stopMusic();
    }

    @Override
    public void dispose() {
//        if (backgroundMusic != null) backgroundMusic.dispose();
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

}
