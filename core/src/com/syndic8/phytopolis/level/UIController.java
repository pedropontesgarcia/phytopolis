package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Texture;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.FilmStrip;

public class UIController {

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
    public UIController() {
        // Nothing here for now.
    }

    /**
     * Gather the assets for this controller.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        waterdropStrip = new FilmStrip(directory.getEntry(
                "gameplay:water_ui",
                Texture.class), 1, 11);
        sunStrip = new FilmStrip(directory.getEntry("gameplay:sun_filmstrip",
                                                    Texture.class), 1, 9);
    }

    /**
     * Updates the UIController.
     *
     * @param waterLvl level of water, in percentage between 0 and 1.
     * @param sunLvl   level of sun, in percentage between 0 and 1.
     */
    public void update(float waterLvl, float sunLvl) {
        waterdropStrip.setFrame(Math.round(
                (waterdropStrip.getSize() - 1) * waterLvl));
        sunStrip.setFrame(Math.round((sunStrip.getSize() - 1) * sunLvl));
    }

    /**
     * Draws the UIController to the canvas.
     *
     * @param c the canvas to draw on.
     */
    public void draw(GameCanvas c) {
        int w = c.getWidth();
        int h = c.getHeight();
        float widthRatio = (float) waterdropStrip.getRegionHeight() /
                waterdropStrip.getRegionWidth();
        c.drawHud(waterdropStrip,
                  w * (0.55f),
                  h * 0.85f,
                  w * 0.125f / widthRatio/1.5f,
                  w * 0.125f/1.5f);
        c.drawHud(sunStrip,
                  w * 0.33f,
                  h * 0.81f,
                  w * 0.125f / widthRatio,
                  w * 0.125f);
    }

}
