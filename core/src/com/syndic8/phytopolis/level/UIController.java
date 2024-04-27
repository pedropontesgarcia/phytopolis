package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.SharedAssetContainer;
import com.syndic8.phytopolis.util.Tilemap;
import com.syndic8.phytopolis.util.Timer;

public class UIController {

    private final Stage stage;
    private final Timer timer;
    private final Label label;
    private FilmStrip waterdropStrip;

    /**
     * Initializes a UIController.
     */
    public UIController(GameCanvas c, Tilemap tm) {
        timer = new Timer(tm.getTime());
        timer.start();
        stage = new Stage(c.getTextViewport());
        BitmapFont font = SharedAssetContainer.getInstance().uiFont;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        label = new Label("00:00", labelStyle);
        label.setPosition(c.getTextViewport().getWorldWidth() * 0.925f -
                                  label.getWidth() / 2f,
                          c.getTextViewport().getWorldHeight() * 0.875f);
        stage.addActor(label);
    }

    /**
     * Gather the assets for this controller.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        waterdropStrip = new FilmStrip(directory.getEntry("gameplay:water_ui",
                                                          Texture.class),
                                       1,
                                       11);
    }

    /**
     * Updates the UIController.
     *
     * @param waterLvl level of water, in percentage between 0 and 1.
     */
    public void update(float dt, float waterLvl) {
        waterdropStrip.setFrame(Math.round(
                (waterdropStrip.getSize() - 1) * waterLvl));
        timer.updateTime(dt);
        label.setText(timer.toString());
    }

    public void pauseTimer() {
        timer.pause();
    }

    public void startTimer() {
        timer.start();
    }

    public void addTime(float t) {
        timer.addTime(t);
    }

    public void addTime() {
        timer.addTime();
    }

    public void eatTime(float t) {
        timer.eatTime(t);
    }

    public boolean timerDone() {
        return timer.isOver();
    }

    public void reset(Tilemap tm) {
        timer.setTime(tm.getTime());
        timer.start();
    }

    /**
     * Draws the UIController to the canvas.
     *
     * @param c the canvas to draw on.
     */
    public void draw(GameCanvas c) {
        int w = c.getWidth();
        int h = c.getHeight();
        float txWidth = waterdropStrip.getRegionWidth();
        float txHeight = waterdropStrip.getRegionHeight();
        float widthRatio = txWidth / txHeight;
        float txWidthDrawn = w * 0.1f;
        float txHeightDrawn = w * 0.1f / widthRatio;
        c.drawHud(waterdropStrip,
                  w * 0.075f - txWidthDrawn / 2f,
                  h * 0.85f,
                  txWidthDrawn,
                  txHeightDrawn);
        c.endHud();
        stage.draw();
        c.beginHud();
    }

}
