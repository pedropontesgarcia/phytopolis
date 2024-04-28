package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.ScreenListener;
import com.syndic8.phytopolis.util.menu.Menu;
import com.syndic8.phytopolis.util.menu.MenuContainer;
import com.syndic8.phytopolis.util.menu.MenuItem;
import edu.cornell.gdiac.audio.AudioEngine;

public class LevelOverMode extends FadingScreen implements Screen {

    private final Rectangle bounds;
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
    private Texture victory;
    private Texture failure;
    private boolean ready;
    private boolean won;
    private boolean gathered;
    private Music backgroundMusic;
    private AudioEngine audioEngine;
    private Texture rs;
    private MenuContainer menuContainer;

    public LevelOverMode(GameCanvas c) {
        this.ready = false;
        this.bounds = new Rectangle(0, 0, 16, 9);
        gathered = false;
        canvas = c;
        createMenu();
    }

    private void createMenu() {
        Menu menu = new Menu(1, 0, 0.3f, -0.1f, 1, Align.center);
        menuContainer = new MenuContainer(menu, canvas);
        ClickListener exitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ready = true;
                fadeOut(0.5f);
            }
        };
        menu.addItem(new MenuItem("NEXT",
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
            gathered = true;
        }
        //        backgroundMusic = directory.getEntry("newgrowth", Music.class);
        //        backgroundMusic.setLooping(true);
        //        backgroundMusic.play();
    }

    public void setBackgroundMusic(Music m) {
        backgroundMusic = m;
    }

    public void setWon(boolean value) {
        won = value;
    }

    @Override
    public void show() {
        active = true;
        menuContainer.activate();
        fadeIn(0.5f);
        //        if (backgroundMusic != null) backgroundMusic.play();
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
        super.update(delta);
        menuContainer.update(delta);
        //        InputController.getInstance().readInput(bounds, Vector2.Zero.add(1, 1));
        //        if (InputController.getInstance().didSecondary()) {
        //            ready = true;
        //            fadeOut(0.5f);
        //        }
    }

    public void draw() {
        canvas.clear();
        canvas.begin();
        canvas.draw((won ? victory : failure),
                    Color.WHITE,
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight());
        canvas.end();
        menuContainer.draw(canvas);
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
