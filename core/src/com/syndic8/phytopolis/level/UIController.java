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
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Player;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.SharedAssetContainer;
import com.syndic8.phytopolis.util.Tilemap;
import com.syndic8.phytopolis.util.Timer;

public class UIController {

    private final Stage stage;
    private final Label label;
    private final Vector2 projMousePosCache;
    private final InputController ic;
    private final Timer timer;
    private final GameCanvas canvas;
    private final ProgressBar progressBar;
    private FilmStrip current;
    private FilmStrip waterdropStrip;
    private FilmStrip waterdropAdd;
    private FilmStrip waterdropRemove;
    private TextureRegion vignette;
    private Cursor normalCursor;
    private Cursor branchCursor;
    private Cursor leafCursor;
    private Cursor waterCursor;
    private Cursor noWaterCursor;
    private float animProgress;
    private boolean currNotEnough;

    /**
     * Initializes a UIController.
     */
    public UIController(GameCanvas c, Tilemap tilemap) {
        canvas = c;
        timer = new Timer(tilemap.getTime());
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
        progressBar = new ProgressBar(0,
                                      100,
                                      0.5f,
                                      false,
                                      SharedAssetContainer.getInstance()
                                              .getProgressBarSkin());
        progressBar.setSize(progressBar.getWidth() * 2,
                            progressBar.getHeight());
        progressBar.setPosition(c.getTextViewport().getWorldWidth() / 2f -
                                        progressBar.getWidth() / 2f,
                                c.getTextViewport().getWorldHeight() * 0.9f);
        stage.addActor(progressBar);
        animProgress = 0;
        currNotEnough = false;
        initialize();
    }

    private void initialize() {
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
                                                        Texture.class), 1, 19);
        waterdropRemove = new FilmStrip(directory.getEntry("ui:water_remove",
                                                           Texture.class),
                                        1,
                                        20);
        current = waterdropStrip;
        vignette = new TextureRegion(directory.getEntry("ui:vignette",
                                                        Texture.class));
        TextureRegion cursorTexture = new TextureRegion(directory.getEntry(
                "ui:cursor",
                Texture.class));
        TextureRegion branchCursorTexture = new TextureRegion(directory.getEntry(
                "ui:branch-cursor",
                Texture.class));
        TextureRegion leafCursorTexture = new TextureRegion(directory.getEntry(
                "ui:leaf-cursor",
                Texture.class));
        TextureRegion waterCursorTexture = new TextureRegion(directory.getEntry(
                "ui:water-cursor",
                Texture.class));
        TextureRegion noWaterCursorTexture = new TextureRegion(directory.getEntry(
                "ui:no-water-cursor",
                Texture.class));
        Pixmap pixmap = getPixmapFromRegion(branchCursorTexture);
        branchCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap = getPixmapFromRegion(leafCursorTexture);
        leafCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap = getPixmapFromRegion(waterCursorTexture);
        waterCursor = Gdx.graphics.newCursor(pixmap,
                                             pixmap.getWidth() / 2,
                                             pixmap.getHeight() / 2);
        pixmap = getPixmapFromRegion(cursorTexture);
        normalCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap = getPixmapFromRegion(noWaterCursorTexture);
        noWaterCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
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

    /**
     * Updates the UIController.
     *
     * @param dt               delta time.
     * @param waterLvl         level of water, in percentage between 0 and 1.
     * @param hazardController hazard controller.
     * @param timerDeduction   deduction to apply to the timer.
     */
    public void update(float dt,
                       float waterLvl,
                       HazardController hazardController,
                       ResourceController resourceController,
                       Player avatar,
                       int prevWater,
                       float timerDeduction,
                       float fireProgress) {
        updateCursor(hazardController, resourceController, avatar);
        updateTexture(dt, waterLvl, prevWater, resourceController);
        progressBar.setValue(fireProgress);
        progressBar.setVisible(hazardController.findValidFireLocs());
        timer.updateTime(dt + timerDeduction); // 1 SEC PER LEAF BITE
        label.setText(timer.toString());
    }

    private void updateCursor(HazardController hc, ResourceController rc, Player avatar) {
        projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unprojectGame(projMousePosCache);
        // TODO: uncomment this later
//        float avatarX = avatar.getX();
//        float avatarY = avatar.getY();

        if (!Float.isNaN(unprojMousePos.x)) { // make sure we aren't tabbed out

//            float distance = unprojMousePos.dst(avatarX, avatarY);
//            if (distance > 2) {
            if (false) {
                Gdx.graphics.setCursor(normalCursor);
            } else {
                if (hc.hasFire(unprojMousePos)) {
                    if (rc.canExtinguish()) Gdx.graphics.setCursor(waterCursor);
                    else Gdx.graphics.setCursor(noWaterCursor);
                } else if ((ic.isGrowBranchModSet() ||
                        (ic.isGrowLeafModSet() && !ic.isGrowLeafModDown())) &&
                        ic.isGrowBranchModDown()) {
                    if (rc.canGrowBranch()) Gdx.graphics.setCursor(branchCursor);
                    else Gdx.graphics.setCursor(noWaterCursor);
                } else if ((ic.isGrowLeafModSet() ||
                        (ic.isGrowBranchModSet() && !ic.isGrowBranchModDown())) &&
                        ic.isGrowLeafModDown()) {
                    if (rc.canGrowLeaf()) Gdx.graphics.setCursor(leafCursor);
                    else Gdx.graphics.setCursor(noWaterCursor);
                } else {
                    Gdx.graphics.setCursor(normalCursor);
                }
            }
        }
    }

    private void updateTexture(float dt, float waterLvl,
                               int prevWater, ResourceController rc) {
        if (rc.getNotEnough()) {
            if (!currNotEnough) {
                currNotEnough = true;
                animProgress = 0;
            }
            rc.setNotEnough(false);
        }
            if (currNotEnough){
//            if (rc.getCurrWater() == 0) {
                if (animProgress >= 1) {
                    currNotEnough = false;
                    current = waterdropStrip;
                } else {
                    current = waterdropRemove;
                    current.setFrame(0);
                    animProgress += dt * 3;
                }
//            } else {
//                rc.setNotEnough(false);
//            }
        }
        if (prevWater < rc.getCurrWater()) {
            current = waterdropAdd;
            animProgress = 0;
        } else if (prevWater > rc.getCurrWater()) {
            current = waterdropRemove;
            animProgress = 0;
        } else if (animProgress >= 1) {
            current = waterdropStrip;
        } else {
            animProgress += dt * 3;
        }
        if (current == waterdropAdd) {
            current.setFrame((int)((current.getSize() - 1) * waterLvl));
        } else if (current == waterdropRemove) {
            if (!currNotEnough) {
                current.setFrame((int) ((current.getSize() - 1) * waterLvl));
            }
        } else {
            current.setFrame((int)((current.getSize() - 2) * waterLvl) + 1);
        }
    }

    public void pauseTimer() {
        timer.pause();
    }

    public void startTimer() {
        timer.start();
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
        float w = c.getWidth();
        float h = c.getHeight();
        float txWidth = current.getRegionWidth();
        float txHeight = current.getRegionHeight();
        float widthRatio = txWidth / txHeight;
        float txWidthDrawn = w * 0.1f;
        float txHeightDrawn = w * 0.1f / widthRatio;
        c.beginHud();
        c.drawHud(vignette, 0, 0, canvas.getWidth(), canvas.getHeight());
        c.drawHud(current,
                  w * 0.075f - txWidthDrawn / 2f,
                  h * 0.85f,
                  txWidthDrawn,
                  txHeightDrawn);
        c.endHud();
        stage.draw();
    }

}
