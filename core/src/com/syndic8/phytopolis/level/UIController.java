package com.syndic8.phytopolis.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.SharedAssetContainer;
import com.syndic8.phytopolis.util.Tilemap;
import com.syndic8.phytopolis.util.Timer;

public class UIController {

    private final Stage stage;
    private final Timer timer;
    private final Label label;
    private final Vector2 projMousePosCache;
    private final InputController ic;
    private final GameCanvas canvas;
    private FilmStrip current;
    private FilmStrip waterdropStrip;
    private FilmStrip waterdropAdd;
    private FilmStrip waterdropRemove;
    private Cursor branchCursor;
    private Cursor leafCursor;
    private Cursor waterCursor;

    /**
     * Initializes a UIController.
     */
    public UIController(GameCanvas c, Tilemap tm) {
        canvas = c;
        timer = new Timer(tm.getTime());
        timer.start();
        projMousePosCache = new Vector2();
        ic = InputController.getInstance();
        stage = new Stage(c.getTextViewport());
        BitmapFont font = SharedAssetContainer.getInstance().getUIFont();
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
        waterdropStrip = new FilmStrip(directory.getEntry("ui:water_ui",
                                                          Texture.class),
                                       1,
                                       22);
        waterdropAdd = new FilmStrip(directory.getEntry("ui:water_add",
                Texture.class),
                1,
                19);
        waterdropRemove = new FilmStrip(directory.getEntry("ui:water_remove",
                Texture.class),
                1,
                20);
        current = waterdropStrip;
        TextureRegion branchCursorTexture = new TextureRegion(directory.getEntry(
                "ui:branch-cursor",
                Texture.class));
        TextureRegion leafCursorTexture = new TextureRegion(directory.getEntry(
                "ui:leaf-cursor",
                Texture.class));
        TextureRegion waterCursorTexture = new TextureRegion(directory.getEntry(
                "ui:water-cursor",
                Texture.class));
        Pixmap pixmap = getPixmapFromRegion(branchCursorTexture);
        branchCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap = getPixmapFromRegion(leafCursorTexture);
        leafCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap = getPixmapFromRegion(waterCursorTexture);
        waterCursor = Gdx.graphics.newCursor(pixmap,
                                             pixmap.getWidth() / 2,
                                             pixmap.getHeight() / 2);
        pixmap.dispose();
    }

    private Pixmap getPixmapFromRegion(TextureRegion region) {
        if (!region.getTexture().getTextureData().isPrepared()) {
            region.getTexture().getTextureData().prepare();
        }
        Pixmap originalPixmap = region.getTexture()
                .getTextureData()
                .consumePixmap();
        Pixmap cursorPixmap = new Pixmap(64, 64, originalPixmap.getFormat());
        cursorPixmap.drawPixmap(originalPixmap,
                                0,
                                0,
                                originalPixmap.getWidth(),
                                originalPixmap.getHeight(),
                                0,
                                0,
                                cursorPixmap.getWidth(),
                                cursorPixmap.getHeight());
        originalPixmap.dispose(); // Avoid memory leaks
        return cursorPixmap;
    }

    /**
     * Updates the UIController.
     *
     * @param waterLvl level of water, in percentage between 0 and 1.
     */
    public void update(float dt,
                       float waterLvl,
                       HazardController hazardController,
                       boolean addedWater,
                       boolean removedWater,
                       int timerDeductions) {
        updateCursor(hazardController);
        updateTexture(waterLvl, addedWater, removedWater);
        timer.updateTime(dt + timerDeductions); // 1 SEC PER LEAF BITE
        label.setText(timer.toString());
    }

    /**
     * Updates the UI texture
     */
    public void updateTexture(float waterLvl, boolean addedWater, boolean removedWater) {
        if (addedWater) {
            current = waterdropAdd;
            current.setFrame(Math.round(
                    (current.getSize() - 1) * waterLvl));
        } else if (removedWater) {
            current = waterdropRemove;
            current.setFrame(Math.round(
                    (current.getSize() - 1) * waterLvl));
        } else {
            current = waterdropStrip;
            current.setFrame(Math.round(
                    (current.getSize() - 2) * waterLvl) + 1);
        }
    }

    /**
     * Updates the custom cursor
     */
    public void updateCursor(HazardController hazardController) {
        projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unproject(projMousePosCache);
        if (hazardController.hasFire(unprojMousePos)) {
            Gdx.graphics.setCursor(waterCursor);
        } else if ((ic.isGrowBranchModSet() ||
                (ic.isGrowLeafModSet() && !ic.isGrowLeafModDown())) &&
                ic.isGrowBranchModDown()) {
            Gdx.graphics.setCursor(branchCursor);
        } else if ((ic.isGrowLeafModSet() ||
                (ic.isGrowBranchModSet() && !ic.isGrowBranchModDown())) &&
                ic.isGrowLeafModDown()) {
            Gdx.graphics.setCursor(leafCursor);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
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

    public boolean timerDone() {
        return timer.isOver();
    }

    public void reset() {
        timer.reset();
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
        float txWidth = current.getRegionWidth();
        float txHeight = current.getRegionHeight();
        float widthRatio = txWidth / txHeight;
        float txWidthDrawn = w * 0.1f;
        float txHeightDrawn = w * 0.1f / widthRatio;
        c.drawHud(current,
                  w * 0.075f - txWidthDrawn / 2f,
                  h * 0.85f,
                  txWidthDrawn,
                  txHeightDrawn);
        c.endHud();
        stage.draw();
        c.beginHud();
    }

}
