package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.levelselect.LevelBox;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.ScreenListener;
import edu.cornell.gdiac.audio.AudioEngine;

public class LevelSelectMode extends FadingScreen implements Screen {

    private final Rectangle bounds;
    private final int numLevels = 6;
    private final LevelBox[] levelBoxes;
    /**
     * Whether or not this screen is active
     */
    private boolean active;
    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;
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

    public LevelSelectMode() {
        this.ready = false;
        this.bounds = new Rectangle(0, 0, 16, 9);

        //Setup levelboxes
        this.levelBoxes = new LevelBox[numLevels];
        fadeIn(0.5f);
    }

    public void gatherAssets(AssetDirectory directory) {
        background = new FilmStrip(directory.getEntry("lvlsel:background",
                                                      Texture.class), 1, 4);
        lighting = directory.getEntry("lvlsel:lighting", Texture.class);
        rs = directory.getEntry("lvlsel:redsquare", Texture.class);
        //        backgroundMusic = directory.getEntry("newgrowth", Music.class);
        //        backgroundMusic.setLooping(true);
        //        backgroundMusic.play();
    }

    public void setBackgroundMusic(Music m) {
        backgroundMusic = m;
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        //Setup levelboxes
        levelBoxes[0] = new LevelBox(canvas.getWidth() / 5f,
                                     canvas.getHeight() / 3.3f);
        levelBoxes[1] = new LevelBox(canvas.getWidth() / 2.3f,
                                     canvas.getHeight() / 3.3f);
        levelBoxes[2] = new LevelBox(canvas.getWidth() / 1.47f,
                                     canvas.getHeight() / 3.3f);
        for (LevelBox lb : levelBoxes) if (lb != null) lb.setTexture(rs);
    }

    @Override
    public void show() {
        active = true;
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

            if (listener != null && ready && isFadeDone()) {
                listener.exitScreen(this, 0);
            }
        }
    }

    public void update(float delta) {
        super.update(delta);
        //        float mouseX = InputController.getInstance().getMouseX();
        //        float mouseY = InputController.getInstance().getMouseY();
        InputController ic = InputController.getInstance();
        Vector2 projMousePos = new Vector2(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unproject(projMousePos);
        float mouseX = unprojMousePos.x;
        float mouseY = unprojMousePos.y;
        for (LevelBox lb : levelBoxes) {
            if (lb != null) lb.setSelected(lb.inBounds(mouseX, mouseY));
        }
        InputController.getInstance().readInput(bounds, Vector2.Zero.add(1, 1));
        if (getSelectedPot() != -1 &&
                InputController.getInstance().didMousePress()) {
            fadeOut(1);
            ready = true;
        }
        if (ready) backgroundMusic.setVolume(super.getVolume());
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
        canvas.end();
        super.draw(canvas);
    }

    public int getSelectedPot() {
        for (int i = 0; i < numLevels; i++) {
            if (levelBoxes[i] != null && levelBoxes[i].getSelected()) return i;
        }
        return -1;
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
        backgroundMusic.stop();
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
        switch (getSelectedPot()) {
            case 0:
                return "gameplay:lvl1";
            case 1:
                return "gameplay:lvl2";
            case 2:
                return "gameplay:lvl3";
        }
        return "";
    }

}
