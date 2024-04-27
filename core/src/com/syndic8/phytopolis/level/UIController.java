package com.syndic8.phytopolis.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;
import com.syndic8.phytopolis.util.Timer;

public class UIController {

    private final Stage stage;
    private final Timer timer;
    private final Label label;
    /**
     * The waterdrop filmstrip.
     */
    private FilmStrip waterdropStrip;
    /**
     * The sun filmstrip.
     */
    private FilmStrip sunStrip;

    /**
     * Initializes a UIController.
     */
    public UIController(GameCanvas c, Tilemap tm) {
        timer = new Timer(tm.getTime());
        timer.start();
        stage = new Stage(c.getTextViewport());
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(
                "fonts/JBM.ttf"));
        FreeTypeFontGenerator.setMaxTextureSize(4096);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 256;
        parameter.color = Color.WHITE;
        BitmapFont font = generator.generateFont(parameter);
        font.getRegion()
                .getTexture()
                .setFilter(Texture.TextureFilter.Linear,
                           Texture.TextureFilter.Linear);
        font.getData().setScale(0.2f);
        generator.dispose();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        label = new Label("00:00", labelStyle);
        label.setPosition(c.getTextViewport().getWorldWidth() / 2f -
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
        sunStrip = new FilmStrip(directory.getEntry("gameplay:sun_filmstrip",
                                                    Texture.class), 1, 9);
    }

    /**
     * Updates the UIController.
     *
     * @param waterLvl level of water, in percentage between 0 and 1.
     * @param sunLvl   level of sun, in percentage between 0 and 1.
     */
    public void update(float dt, float waterLvl, float sunLvl) {
        waterdropStrip.setFrame(Math.round(
                (waterdropStrip.getSize() - 1) * waterLvl));
        sunStrip.setFrame(Math.round((sunStrip.getSize() - 1) * sunLvl));
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
                  w * 0.6f - txWidthDrawn / 2f,
                  h * 0.85f,
                  txWidthDrawn,
                  txHeightDrawn);
        c.drawHud(sunStrip,
                  w * 0.4f - txWidthDrawn / 2f,
                  h * 0.85f,
                  txWidthDrawn,
                  txHeightDrawn);
        c.endHud();
        stage.draw();
        c.beginHud();
    }

}
