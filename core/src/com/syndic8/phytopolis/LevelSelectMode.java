package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
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

import java.util.Arrays;

import static com.syndic8.phytopolis.GDXRoot.ExitCode;

public class LevelSelectMode extends FadingScreen implements Screen {

    private final int numLevels = 6;
    private final int numScreens = 2;
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
    private Texture background;
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
    private FilmStrip[] pots;
    private levelState[] levelStates;
    private boolean screen1;
    private boolean swapping;
    private Texture lighting7to12;
    private Texture background7to12;
    private final float fadeTime = 0.3f;
    private Texture easytext;
    private Texture hardtext;

    public LevelSelectMode(GameCanvas c) {
        canvas = c;
        ready = false;

        //Setup levelboxes
        levelBoxes = new LevelBox[numLevels];
        projMousePosCache = new Vector2();
        createMenu();
        gathered = false;
        this.soundController = SoundController.getInstance();
        this.levelStates = new levelState[12];
        this.screen1 = true;
        this.swapping = false;
        Arrays.fill(levelStates, levelState.BEATEN);
    }

    private void createMenu() {
        Menu menu = new Menu(2,
                             -0.12f,
                             0.4f,
                             -0.3f,
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
        ClickListener swapListener = new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fadeOut(fadeTime);
                swapping = true;
            }
        };
        menu.addItem(new MenuItem("MORE",
                1,
                swapListener,
                menu,
                menuContainer,
                canvas));
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
            background = directory.getEntry("lvlsel:background1to6",
                                                          Texture.class);
            background7to12 = directory.getEntry("lvlsel:background7to12",
                    Texture.class);
            lighting = directory.getEntry("lvlsel:lighting", Texture.class);
            lighting7to12 = directory.getEntry("lvlsel:lighting7to12", Texture.class);
            easytext = directory.getEntry("lvlsel:easytext", Texture.class);
            hardtext = directory.getEntry("lvlsel:hardtext", Texture.class);
            rs = directory.getEntry("lvlsel:redsquare", Texture.class);

            //pots
            pots = new FilmStrip[numLevels * numScreens];
            pots[0] = new FilmStrip(directory.getEntry("lvlsel:pot1", Texture.class), 1, 5);
            pots[1] = new FilmStrip(directory.getEntry("lvlsel:pot2", Texture.class), 1, 5);
            pots[2] = new FilmStrip(directory.getEntry("lvlsel:pot3", Texture.class), 1, 5);
            pots[3] = new FilmStrip(directory.getEntry("lvlsel:pot4", Texture.class), 1, 5);
            pots[4] = new FilmStrip(directory.getEntry("lvlsel:pot5", Texture.class), 1, 5);
            pots[5] = new FilmStrip(directory.getEntry("lvlsel:pot6", Texture.class), 1, 5);
            //Hard level pots
            pots[6] = new FilmStrip(directory.getEntry("lvlsel:pot7", Texture.class), 1, 5);
            pots[7] = new FilmStrip(directory.getEntry("lvlsel:pot8", Texture.class), 1, 5);
            pots[8] = new FilmStrip(directory.getEntry("lvlsel:pot9", Texture.class), 1, 5);
            pots[9] = new FilmStrip(directory.getEntry("lvlsel:pot10", Texture.class), 1, 5);
            pots[10] = new FilmStrip(directory.getEntry("lvlsel:pot11", Texture.class), 1, 5);
            pots[11] = new FilmStrip(directory.getEntry("lvlsel:pot12", Texture.class), 1, 5);
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
        float xOff = -1.65f;
        float yOff = -2.3f;
        levelBoxes[0] = new LevelBox(canvas.getWidth() / 5f,
                                     canvas.getHeight() / 3.3f);
        levelBoxes[1] = new LevelBox(canvas.getWidth() / 2.3f,
                                     canvas.getHeight() / 3.3f);
        levelBoxes[2] = new LevelBox(canvas.getWidth() / 1.47f,
                                     canvas.getHeight() / 3.3f);
        levelBoxes[3] = new LevelBox((canvas.getWidth() / 5f) + xOff,
                (canvas.getHeight() / 3.3f) + yOff);
        levelBoxes[4] = new LevelBox((canvas.getWidth() / 2.3f) + xOff,
                (canvas.getHeight() / 3.3f) + yOff);
        levelBoxes[5] = new LevelBox((canvas.getWidth() / 1.47f) + xOff,
                (canvas.getHeight() / 3.3f) + yOff);
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
        if(getFadeState() == Fade.HIDDEN && swapping){
            screen1 = !screen1;
            fadeIn(fadeTime);
            swapping = false;
        }
        if (ready && exitCode == ExitCode.EXIT_LEVELS)
            soundController.setMusicVolume(super.getVolume());
    }

    public void draw() {
        canvas.clear();
        canvas.begin();
        //Draw background
        if(screen1) {
            canvas.draw(background,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
            canvas.draw(easytext,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());

        }else {
            canvas.draw(background7to12,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
            canvas.draw(hardtext,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
        }
        //Next, draw pots
        int selectedPot = getSelectedPot();
        int iOff = 0;
        if (!screen1) iOff = 6;
        for (int i = iOff; i < 6 + iOff; i++){

            switch(levelStates[i]){
                case LOCKED:
                    pots[i].setFrame(0);
                    break;
                case BEATEN:
                    if(i - iOff == selectedPot) pots[i].setFrame(4);
                    else pots[i].setFrame(3);
                    break;
                case UNLOCKED:
                    if(i - iOff == selectedPot) pots[i].setFrame(2);
                    else pots[i].setFrame(1);
                    break;
            }
            canvas.draw(pots[i],
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
        }

        //Finally, draw lighting
        canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);
        if(screen1) canvas.draw(lighting,
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
        else canvas.draw(lighting7to12,
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
        for (int i = numLevels - 1; i >= 0; i--) {
            if (levelBoxes[i] != null && levelBoxes[i].getSelected()) return i;
        }
        return -1;
    }

    public void setLevel() {
        if (screen1) {
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
                case 3:
                    level = "gameplay:lvl4";
                    break;
                case 4:
                    level = "gameplay:lvl5";
                    break;
                case 5:
                    level = "gameplay:lvl6";
                    break;
            }
        }else{
            switch (getSelectedPot()) {
                case 0:
                    level = "gameplay:lvl7";
                    break;
                case 1:
                    level = "gameplay:lvl8";
                    break;
                case 2:
                    level = "gameplay:lvl9";
                    break;
                case 3:
                    level = "gameplay:lvl10";
                    break;
                case 4:
                    level = "gameplay:lvl11";
                    break;
                case 5:
                    level = "gameplay:lvl12";
                    break;
            }
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

    public enum levelState {LOCKED, UNLOCKED, BEATEN};

}
