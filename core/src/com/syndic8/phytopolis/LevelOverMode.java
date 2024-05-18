package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.ScreenListener;
import com.syndic8.phytopolis.util.SharedAssetContainer;
import com.syndic8.phytopolis.util.menu.Menu;
import com.syndic8.phytopolis.util.menu.MenuContainer;
import com.syndic8.phytopolis.util.menu.MenuItem;
import edu.cornell.gdiac.audio.AudioEngine;
import edu.cornell.gdiac.audio.AudioSource;

public class LevelOverMode extends FadingScreen implements Screen {

    /**
     * Reference to GameCanvas created by the root
     */
    private final GameCanvas canvas;
    private final SoundController soundController;
    private final GameplayMode gameplayMode;
    private final Vector2 cameraVector;
    private final Vector3 initialCamera;
    private final Vector3 targetCamera;
    private final Vector3 interpolationCache;
    private final float TRANSITION_DURATION = 3f;
    private final TextButton label;
    private final Stage stage;
    /**
     * Whether or not this screen is active
     */
    private boolean active;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;
    private Texture victory;
    private Texture failure;
    private boolean ready;
    private boolean won;
    private boolean gathered;
    private int victoryMusic;
    private AudioEngine audioEngine;
    private Texture rs;
    private MenuContainer menuContainer;
    private int lossMusic;
    private float tmr;
    private float transitionProgress;
    private boolean transitionDone;

    public LevelOverMode(GameCanvas c, GameplayMode gm) {
        ready = false;
        gathered = false;
        transitionDone = false;
        tmr = 0;
        cameraVector = new Vector2();
        initialCamera = new Vector3();
        targetCamera = new Vector3();
        interpolationCache = new Vector3();
        stage = new Stage(c.getTextViewport());
        BitmapFont font = SharedAssetContainer.getInstance().getUIFont(3);
        TextButton.TextButtonStyle labelStyle = new TextButton.TextButtonStyle();
        labelStyle.font = font;
        label = new TextButton("VICTORY!", labelStyle);
        label.setStyle(label.getStyle());
        float yOff = c.getTextViewport().getWorldHeight() * 0.1f;
        label.setPosition(c.getTextViewport().getWorldWidth() / 2f -
                                  label.getWidth() / 2f,
                          c.getTextViewport().getWorldHeight() / 2f -
                                  label.getHeight() / 2f + yOff);
        stage.addActor(label);
        gameplayMode = gm;
        canvas = c;
        createMenu();
        this.soundController = SoundController.getInstance();
    }

    private void createMenu() {
        Menu menu = new Menu(1,
                             0,
                             0.3f,
                             -0.1f,
                             1,
                             Align.center,
                             Menu.DEFAULT_WIDTH);
        menuContainer = new MenuContainer(menu, canvas);
        ClickListener exitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                ready = true;
                menuContainer.deactivate();
                fadeOut(0.5f);
                doVolumeFade(true);
            }
        };
        menu.addItem(new MenuItem("BACK TO\nLEVELS",
                                  0,
                                  exitListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menuContainer.populate();
    }

    public void gatherAssets(AssetDirectory directory) {
        if (!gathered) {
            victory = directory.getEntry("over:victory", Texture.class);
            failure = directory.getEntry("over:failure", Texture.class);
            AudioSource bgm = directory.getEntry("levelcomplete",
                                                 AudioSource.class);
            victoryMusic = soundController.addMusic(bgm);
            bgm = directory.getEntry("gameover", AudioSource.class);
            lossMusic = soundController.addMusic(bgm);
            if (won) {
                soundController.setMusic(victoryMusic);
            } else {
                soundController.setMusic(lossMusic);
            }
            soundController.setLooping(true);
            soundController.playMusic();
            gathered = true;
        }
    }

    public void setWon(boolean value) {
        won = value;
    }

    @Override
    public void show() {
        active = true;
        tmr = 0;
        menuContainer.activate();
        doVolumeFade(true);
        fadeIn(0.5f);
        float aspectRatio = canvas.getWidth() / canvas.getHeight();
        float cameraX = gameplayMode.getTilemap().getWorldWidth() / 2f;
        float initialCameraY =
                gameplayMode.getTilemap().getWorldWidth() / aspectRatio / 2f;
        float targetCameraY =
                gameplayMode.getBackgroundHeight() - initialCameraY;
        initialCamera.set(cameraX, initialCameraY, 0);
        targetCamera.set(cameraX, targetCameraY, 0);
        cameraVector.set(initialCamera.x, initialCamera.y);
        interpolationCache.set(initialCamera);
        canvas.cameraUpdate(cameraVector, false);
        if (won) {
            soundController.setMusic(victoryMusic);
        } else {
            soundController.setMusic(lossMusic);
        }
        soundController.setLooping(true);
        soundController.playMusic();
        label.addAction(Actions.alpha(0));
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (listener != null && ready && isFadeDone()) {
                menuContainer.deactivate();
                listener.exitScreen(this, 0);
            }
        }
    }

    public void update(float delta) {
        if (gameplayMode.getFadeState() != Fade.SHOWN) gameplayMode.fadeIn(0);
        super.update(delta);
        tmr += delta;
        transitionProgress = Math.min(1, tmr / TRANSITION_DURATION);
        interpolationCache.set(initialCamera);
        interpolationCache.interpolate(targetCamera,
                                       transitionProgress,
                                       Interpolation.smooth2);
        cameraVector.set(interpolationCache.x, interpolationCache.y);
        soundController.setActualMusicVolume(
                super.getVolume() * soundController.getUserMusicVolume());
        if (transitionProgress == 1 || !won) {
            menuContainer.update(delta);
            stage.act(delta);
        } else {
            transitionDone = false;
        }
        if (transitionProgress == 1 && !transitionDone) {
            transitionDone = true;
            label.addAction(Actions.alpha(1, 0.5f, Interpolation.linear));
        }
    }

    public void draw() {
        canvas.cameraUpdate(cameraVector, false);
        canvas.clear();
        gameplayMode.setCameraVector(cameraVector);
        gameplayMode.drawLevelOver();
        canvas.begin();
        if (!won) canvas.draw(failure,
                              Color.WHITE,
                              0,
                              0,
                              canvas.getWidth(),
                              canvas.getHeight());
        canvas.end();
        if (transitionProgress == 1 || !won) {
            menuContainer.draw(canvas);
            if (won) stage.draw();
        }
        super.draw(canvas);
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
        //        backgroundMusic.stop();
    }

    @Override
    public void dispose() {
        //        backgroundMusic.dispose();
    }

    public void reset() {
        ready = false;
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

}
