/*
 * PlatformController.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.*;
import com.syndic8.phytopolis.level.models.*;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.OSUtils;
import com.syndic8.phytopolis.util.Tilemap;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.SoundEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for the gameplay mode.
 */
public class GameplayMode extends WorldController {

    private final Vector2 cameraVector;
    private final Vector2 projMousePosCache;
    private final InputController ic;
    private final float lvl1LeafWidth = 1.4f;
    private final float lvl2LeafWidth = 1.2f;
    private final float lvl3LeafWidth = 0.9f;
    private final SoundController soundController;
    private final float timeSinceUIUpdate = 0;
    private final Color bgColor = new Color(Color.WHITE);
    protected Texture jumpTexture;
    private PlantController plantController;
    private HazardController hazardController;
    private ResourceController resourceController;
    private SunController sunController;
    private UIController uiController;
    private FilmStrip jumpAnimator;
    private Texture jogTexture;
    private FilmStrip jogAnimator;
    private FilmStrip idleAnimator;
    private boolean gathered;
    private Texture background;
    private Texture avatarTexture;
    private Tilemap tilemap;
    private JsonValue constants;
    private Player avatar;
    private int backgroundMusic;
    private String lvl;
    private CollisionController collisionController;
    private int plantSound;
    //private int leafSound;
    private int boingSound;
    private Texture sunIndicatorTexture;
    private Texture waterIndicatorTexture;
    private Texture minusWaterIndicatorTexture;
    private int sunSound;
    private int waterCollectSound;
    private int bugStompSound;
    private float timeSinceGrow;
    private int numBranchesSinceGrow;
    private float timeSpent;
    private float bestTime;
    private Tilemap.TilemapParams tilemapParams;

    /**
     * Creates and initialize a new instance of the game.
     */
    public GameplayMode(GameCanvas c) {
        super();
        cameraVector = new Vector2();
        projMousePosCache = new Vector2();
        gathered = false;
        ic = InputController.getInstance();
        canvas = c;
        this.soundController = SoundController.getInstance();
        this.backgroundMusic = -1;
        timeSinceGrow = 1.1f;
        timeSpent = 0;
    }

    public void setLevel(String lvl) {
        this.lvl = lvl;
    }

    public Tilemap getTilemap() {
        return tilemap;
    }

    public Texture getSunIndicatorTexture() {
        return sunIndicatorTexture;
    }

    /**
     * Gather the assets for this controller.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        JsonValue level = directory.getEntry(lvl, JsonValue.class);
        tilemap = new Tilemap(level, canvas);
        tilemap.gatherAssets(directory);
        canvas.setWorldSize(tilemap.getWorldWidth());

        setBounds(tilemap.getWorldWidth(), tilemap.getWorldHeight());

        AudioSource bgm = getLevelMusic(directory);
        backgroundMusic = soundController.addMusic(bgm);
        soundController.setMusic(backgroundMusic);
        soundController.setLooping(true);
        soundController.setActualMusicVolume(soundController.getUserMusicVolume());
        soundController.playMusic();
        soundController.pauseMusic();
        SoundEffect plantSoundEffect = directory.getEntry("growbranch2",
                                                          SoundEffect.class);
        plantSound = soundController.addSoundEffect(plantSoundEffect);
        //        SoundEffect leafSoundEffect = directory.getEntry("growleaf",
        //                                                         SoundEffect.class);
        //        leafSound = soundController.addSoundEffect(leafSoundEffect);
        boingSound = soundController.addSoundEffect(directory.getEntry(
                "bouncyleafboing",
                SoundEffect.class));
        sunSound = SoundController.getInstance()
                .addSoundEffect(directory.getEntry("suncollectsound",
                                                   SoundEffect.class));
        waterCollectSound = SoundController.getInstance()
                .addSoundEffect(directory.getEntry("watercollectsound",
                                                   SoundEffect.class));
        bugStompSound = SoundController.getInstance()
                .addSoundEffect(directory.getEntry("bugstomp",
                                                   SoundEffect.class));

        background = directory.getEntry(tilemap.getBackground(), Texture.class);

        uiController = new UIController(canvas, tilemap);
        uiController.gatherAssets(directory);

        resourceController = new ResourceController();
        float branchHeight = tilemap.getTileHeight();
        int plantNodesPerRow = Math.round(
                (tilemap.getTilemapWidth() - 2) * (float) Math.sqrt(3));
        float plantWidth =
                branchHeight * (float) Math.sqrt(3) * (plantNodesPerRow - 1) /
                        2;
        float plantXOrigin = bounds.width / 2 - plantWidth / 2;
        List<Float> plantXPositions = new ArrayList<Float>();
        for (int i = 0; i < plantNodesPerRow; i++) {
            plantXPositions.add(
                    plantXOrigin + i * plantWidth / (plantNodesPerRow - 1));
        }

        plantController = new PlantController(resourceController);
        hazardController = new HazardController(plantController,
                                                (int) tilemap.getFireRate(),
                                                2,
                                                8,
                                                6,
                                                10,
                                                tilemap);
        sunController = new SunController(5,
                                          10,
                                          tilemap.getWorldHeight(),
                                          plantXPositions);
        plantController.gatherAssets(directory);
        hazardController.gatherAssets(directory);
        sunController.gatherAssets(directory);

        if (!gathered) {
            gathered = true;
            avatarTexture = directory.getEntry("gameplay:player",
                                               Texture.class);
            jumpTexture = directory.getEntry("jump", Texture.class);
            jumpAnimator = new FilmStrip(jumpTexture, 1, 13, 13);

            jogTexture = directory.getEntry("jog", Texture.class);
            jogAnimator = new FilmStrip(jogTexture, 1, 8, 8);

            idleAnimator = new FilmStrip(directory.getEntry("idle",
                                                            Texture.class),
                                         1,
                                         3,
                                         3);
            sunIndicatorTexture = directory.getEntry("ui:sun_indicator",
                                                     Texture.class);
            waterIndicatorTexture = directory.getEntry("ui:water_indicator",
                                                       Texture.class);
            minusWaterIndicatorTexture = directory.getEntry(
                    "ui:minus_water_indicator",
                    Texture.class);

            constants = directory.getEntry("gameplay:constants",
                                           JsonValue.class);

        }
    }

    private AudioSource getLevelMusic(AssetDirectory directory) {
        switch (lvl) {
            case "gameplay:lvl1":
            case "gameplay:lvl2":
            case "gameplay:lvl3":
                return directory.getEntry("viridian", AudioSource.class);
            case "gameplay:lvl4":
            case "gameplay:lvl5":
            case "gameplay:lvl6":
                return directory.getEntry("ozonelayer", AudioSource.class);
            case "gameplay:lvl7":
            case "gameplay:lvl8":
            case "gameplay:lvl9":
                return directory.getEntry("katabasis", AudioSource.class);
            case "gameplay:lvl10":
            case "gameplay:lvl11":
            case "gameplay:lvl12":
                return directory.getEntry("sunrise", AudioSource.class);
            default:
                return directory.getEntry("viridian", AudioSource.class);
        }
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        super.show();
        if (soundController.getMusicQueuePos() != backgroundMusic) {
            soundController.setMusic(backgroundMusic);
        }
        if (!soundController.isMusicPlaying()) {
            soundController.setActualMusicVolume(soundController.getUserMusicVolume());
            soundController.playMusic();
        }
        uiController.startTimer();
        setPaused(false);
    }

    /**
     * Returns whether to process the update loop
     * <p>
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }
        if (getFadeState() == Fade.HIDDEN) {
            doVolumeFade(false);
            fadeIn(0.5f);
        }

        if (!isFailure() && uiController.timerDone()) {
            setFailure(true);
            fadeOut(1f);
            doVolumeFade(true);
            return false;
        }

        return true;
    }

    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        //        if (dt > 0.02f) System.out.println(dt);
        if (!isComplete()) {
            timeSpent += dt;
        }
        if (getFadeState() == Fade.FADE_OUT)
            soundController.setActualMusicVolume(
                    super.getVolume() * soundController.getUserMusicVolume());
        int water = resourceController.getCurrWater();
        hazardController.update(dt);
        // Process actions in object model
        avatar.setMovement(ic.getHorizontal() * avatar.getForce());
        avatar.setJumping(ic.didJump());
        if (ic.getHorizontal() != 0 || ic.didJump() || ic.isDropKeyDown()) {
            ic.resetScrolled();
        }
        timeSinceGrow += dt;
        processPlantGrowth();

        avatar.applyForce();

        if (ic.didScrollReset()) {
            ic.resetScrolled();
        }
        ic.setHeight(tilemap.getTilemapHeight() * tilemap.getTileHeight() -
                             canvas.getHeight());
        float aspectRatio = canvas.getWidth() / canvas.getHeight();
        float cameraHeight = tilemap.getWorldWidth() / aspectRatio;
        //        cameraVector.set(tilemap.getWorldWidth() / 2f,
        //                         Math.max(cameraHeight / 2f,
        //                                  Math.min(tilemap.getTilemapHeight() *
        //                                                   tilemap.getTileHeight() -
        //                                                   cameraHeight / 2f,
        //                                           avatar.getY()) + ic.getScrolled()));
        cameraVector.set(tilemap.getWorldWidth() / 2f,
                         Math.max(cameraHeight / 2f,
                                  Math.min(avatar.getY() + ic.getScrolled(),
                                           tilemap.getWorldHeight() -
                                                   cameraHeight / 2f)));

        // generate hazards please
        for (Model m : objects) {
            if (m instanceof Water) {
                ((Water) m).regenerate(dt);
                m.update(dt);
            }
            if (m instanceof Sun) {
                ((Sun) m).update(dt,
                                 m.getY() <
                                         plantController.getMaxPlantHeight() -
                                                 resourceController.SUN_TOLERANCE);

            }
            if (m instanceof Hazard) {
                m.update(dt);
            }
        }
        Sun s = sunController.spawnSuns(dt, tilemap);
        if (s != null) addObject(s);
        for (Hazard h : hazardController.updateHazards(dt)) {
            addObject(h);
        }
        projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unprojectGame(projMousePosCache);
        if (ic.didMousePress() && hazardController.hasFire(unprojMousePos)) {
            hazardController.extinguishFire(unprojMousePos, avatar);
        }
        hazardController.deleteFireBugs(plantController.propagateDestruction(dt));

        //        if (timeSinceUIUpdate >= 1) {
        uiController.update(dt,
                            resourceController.getCurrRatio(),
                            plantController,
                            hazardController,
                            resourceController,
                            avatar,
                            water,
                            plantController.countTimerDeductions(),
                            hazardController.getFireProgress(),
                            tilemap);
        collisionController.setAddedWater(false);
        //        } else {
        //            timeSinceUIUpdate += 0.05;
        //        }
        // Check for win condition
        if ((plantController.getMaxPlantHeight() >
                tilemap.getVictoryHeight() * tilemap.getTileHeight()) &&
                !isComplete()) {

            setComplete(true);
            fadeOut(1f);
            doVolumeFade(true);
            uiController.pauseTimer();
            FileHandle saveFile = Gdx.files.absolute(OSUtils.getSaveFile());
            JsonReader saveJsonReader = new JsonReader();
            JsonValue saveJson = saveJsonReader.parse(saveFile);
            int lastBeaten = Math.max(saveJson.getInt("lastBeaten"),
                                      tilemap.getLevelNumber() - 1);
            saveJson.get("lastBeaten").set(lastBeaten, null);
            float currBest = saveJson.getFloat(
                    "bestTime" + tilemap.getLevelNumber());
            bestTime =
                    currBest == -1 ? timeSpent : Math.min(currBest, timeSpent);
            saveJson.get("bestTime" + tilemap.getLevelNumber())
                    .set(bestTime, null);
            saveFile.writeString(saveJson.prettyPrint(JsonWriter.OutputType.json,
                                                      0), false);
        }
    }

    /**
     * Processes plant growth using player input. Grows a branch in the
     * corresponding direction at the node closest to the player's position.
     */
    public void processPlantGrowth() {
        // get mouse position
        projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unprojectGame(projMousePosCache);

        float avatarX = avatar.getX();
        float avatarY = avatar.getY();
        float distance = unprojMousePos.dst(avatarX, avatarY);
        if (distance > tilemap.getTileHeight() * uiController.getRangeScale())
            return;

        boolean canGrowBranch = ic.didGrowBranch() && ic.isGrowBranchModDown();
        boolean canGrowLeaf = ic.didGrowLeaf() && ic.isGrowLeafModDown();
        boolean noPriority = ic.isGrowLeafModSet() == ic.isGrowBranchModSet();
        boolean doesLeafHavePriority =
                ic.isGrowLeafModSet() && !ic.isGrowBranchModSet();
        boolean shouldGrowBranch = false;
        boolean shouldGrowLeaf = false;
        if (noPriority && canGrowBranch && canGrowLeaf) {
            shouldGrowBranch = true;
            shouldGrowLeaf = true;
        } else if (((doesLeafHavePriority ||
                (!doesLeafHavePriority && !canGrowBranch)) && canGrowLeaf)) {
            shouldGrowLeaf = true;
        } else if (((!doesLeafHavePriority) ||
                (doesLeafHavePriority && !canGrowLeaf)) && canGrowBranch) {
            shouldGrowBranch = true;
        }

        if (!hazardController.hasFire(unprojMousePos)) {
            if (shouldGrowBranch) {
                if (timeSinceGrow >= 0.25f || numBranchesSinceGrow < 1) {
                    Branch branch = plantController.growBranch(unprojMousePos.x,
                                                               unprojMousePos.y);
                    if (branch != null) {
                        if (timeSinceGrow >= 0.25f) {
                            timeSinceGrow = 0;
                            numBranchesSinceGrow = 0;
                        }
                        numBranchesSinceGrow++;
                        addObject(branch);
                        Vector2 branchCenter = getBranchCenter(branch);
                        addObject(new Indicator(branchCenter.x,
                                                branchCenter.y,
                                                getMinusWaterIndicatorTexture(),
                                                tilemap.getTilemapParams(),
                                                0.5f));
                        soundController.playSound(plantSound);
                    }
                }
            }
            if (shouldGrowLeaf) {
                Leaf.leafType lt = getLevelLeafType();
                float width = getLevelLeafWidth();
                Model newLeaf = plantController.growLeaf(unprojMousePos.x,
                                                         unprojMousePos.y +
                                                                 0.5f *
                                                                         tilemap.getTileHeight(),
                                                         lt,
                                                         width);
                if (newLeaf != null) {
                    addObject(newLeaf);
                    addObject(new Indicator(newLeaf.getX(),
                                            newLeaf.getY(),
                                            getMinusWaterIndicatorTexture(),
                                            tilemap.getTilemapParams(),
                                            0.5f));
                }

            }
        }
    }

    private Vector2 getBranchCenter(Branch branch) {
        return new Vector2((float) (branch.getX() -
                tilemap.getTileHeight() / 2f * Math.sin(branch.getAngle())),
                           (float) (branch.getY() +
                                   tilemap.getTileHeight() / 2f *
                                           Math.cos(branch.getAngle())));
    }

    public Texture getMinusWaterIndicatorTexture() {
        return minusWaterIndicatorTexture;
    }

    private Leaf.leafType getLevelLeafType() {
        switch (lvl) {
            case "gameplay:lvl1":
            case "gameplay:lvl2":
            case "gameplay:lvl3":
            case "gameplay:lvl4":
            case "gameplay:lvl5":
            case "gameplay:lvl6":
                return Leaf.leafType.NORMAL;
            case "gameplay:lvl7":
            case "gameplay:lvl8":
            case "gameplay:lvl9":
                return Leaf.leafType.NORMAL1;
            case "gameplay:lvl10":
            case "gameplay:lvl11":
            case "gameplay:lvl12":
                return Leaf.leafType.NORMAL2;
            default:
                return Leaf.leafType.NORMAL;
        }
    }

    private float getLevelLeafWidth() {
        switch (lvl) {
            case "gameplay:lvl1":
            case "gameplay:lvl2":
            case "gameplay:lvl3":
                return lvl1LeafWidth;
            case "gameplay:lvl4":
            case "gameplay:lvl5":
            case "gameplay:lvl6":
                return lvl2LeafWidth;
            case "gameplay:lvl7":
            case "gameplay:lvl8":
            case "gameplay:lvl9":
            case "gameplay:lvl10":
            case "gameplay:lvl11":
            case "gameplay:lvl12":
                return lvl3LeafWidth;
            default:
                return lvl1LeafWidth;
        }
    }

    /**
     * Draw the physics objects to the canvas
     * <p>
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     * <p>
     * The method draws all objects in the order that they were added.
     */
    public void draw() {
        canvas.clear();
        canvas.cameraUpdate(cameraVector, true);
        canvas.beginGame();
        drawBackground();
        tilemap.draw(canvas);
        super.draw();
        if (!isPaused()) {
            projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
            Vector2 unprojMousePos = canvas.unprojectGame(projMousePosCache);
            float avatarX = avatar.getX();
            float avatarY = avatar.getY();
            float distance = unprojMousePos.dst(avatarX, avatarY);
            if (distance <= tilemap.getTileHeight() * 2) {
                if (!hazardController.hasFire(unprojMousePos)) {
                    if (!ic.isGrowLeafModDown()) {
                        plantController.drawGhostBranch(canvas,
                                                        unprojMousePos.x,
                                                        unprojMousePos.y);
                    } else {
                        Leaf.leafType lt = getLevelLeafType();
                        float width = getLevelLeafWidth();
                        plantController.drawGhostLeaf(canvas,
                                                      lt,
                                                      width,
                                                      unprojMousePos.x,
                                                      unprojMousePos.y + 0.5f *
                                                              tilemap.getTileHeight());
                    }

                }
            }
        }
        hazardController.drawWarning(canvas, cameraVector);
        plantController.drawGlow(canvas);
        canvas.end();
        uiController.draw(canvas);
        super.draw(canvas);
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * We need this method to stop all sounds when we pause.
     * Pausing happens when we switch game modes.
     */
    public void pause() {
        //jumpSound.stop(jumpId);
        //plopSound.stop(plopId);
        //fireSound.stop(fireId);
        setPaused(true);
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        super.hide();
        //soundController.stopMusic();
        uiController.pauseTimer();
    }

    public void drawBackground() {
        if (background != null) {
            float aspectRatio = canvas.getWidth() / canvas.getHeight();
            float initialCameraY = tilemap.getWorldWidth() / aspectRatio / 2f;
            float y = cameraVector.y - initialCameraY;

            float parallaxFactor = 0.5f; // Adjust this value to control the parallax effect
            float parallaxOffset = y * parallaxFactor;

            // Compress the height based on the parallax factor
            float compressedHeight = getBackgroundHeight() -
                    ((getBackgroundHeight()) * parallaxFactor) + initialCameraY;
            canvas.draw(background,
                        Color.WHITE,
                        -0.1f,
                        parallaxOffset,
                        getBackgroundWidth(),
                        compressedHeight); // Use compressed height
        }
    }

    public float getBackgroundHeight() {
        return tilemap.getWorldWidth() * background.getHeight() /
                background.getWidth();
    }

    public float getBackgroundWidth() {
        return tilemap.getWorldWidth() * 1.05f;
    }

    /**
     * Draw the physics objects to the canvas
     * <p>
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     * <p>
     * The method draws all objects in the order that they were added.
     */
    public void drawLevelOver(float bgAlpha) {
        canvas.clear();
        canvas.beginGame();
        drawBackground(bgAlpha);
        tilemap.drawLevelOver(canvas);
        super.drawLevelOver();
        canvas.end();
        super.draw(canvas);
    }

    public void drawBackground(float bgAlpha) {
        if (background != null) {
            float aspectRatio = canvas.getWidth() / canvas.getHeight();
            float initialCameraY = tilemap.getWorldWidth() / aspectRatio / 2f;
            float y = cameraVector.y - initialCameraY;

            float parallaxFactor = 0.5f; // Adjust this value to control the parallax effect
            float parallaxOffset = y * parallaxFactor;

            // Compress the height based on the parallax factor
            float compressedHeight = getBackgroundHeight() -
                    ((getBackgroundHeight()) * parallaxFactor) + initialCameraY;
            bgColor.set(bgColor.r, bgColor.g, bgColor.b, bgAlpha);
            canvas.draw(background,
                        bgColor,
                        -0.1f,
                        parallaxOffset,
                        getBackgroundWidth(),
                        compressedHeight); // Use compressed height
        }
    }

    public float getTimeSpent() {
        return timeSpent;
    }

    public float getBestTime() {
        return bestTime;
    }

    public void setCameraVector(Vector2 vector) {
        cameraVector.set(vector.x, vector.y);
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        timeSinceGrow = 1.1f;
        timeSpent = 0;
        Vector2 gravity = new Vector2(world.getGravity());
        for (Model obj : objects) {
            if (obj instanceof GameObject) {
                ((GameObject) obj).deactivatePhysics(world);
            }
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        //        soundController.stopMusic();
        //        soundController.playMusic();
        uiController.reset(tilemap.getTime());

        ic.resetScrolled();

        resourceController.reset();

        soundController.rewindMusic();

        world = new World(gravity, false);
        tilemap.populateLevel(this);
        tilemapParams = tilemap.getTilemapParams();
        populateBoundaryWalls();
        float branchHeight = tilemap.getTileHeight();
        int plantNodesPerRow = Math.round(
                (tilemap.getTilemapWidth() - 2) * (float) Math.sqrt(3));
        float plantWidth =
                branchHeight * (float) Math.sqrt(3) * (plantNodesPerRow - 1) /
                        2;
        float plantXOrigin = bounds.width / 2 - plantWidth / 2;
        plantController.reset(world, tilemapParams);

        hazardController.reset((int) tilemap.getFireRate(),
                               2,
                               8,
                               6,
                               10,
                               tilemap);
        sunController.reset(tilemap.getWorldHeight(),
                            plantController.getPlantXPositions());
        setComplete(false);
        setFailure(false);
    }

    /**
     * Lays out the boundary walls around the level.
     */
    private void populateBoundaryWalls() {
        JsonValue defaults = constants.get("defaults");

        // Add floor
        PolygonObject obj;
        obj = new PolygonObject(new float[]{0,
                0,
                tilemap.getWorldWidth(),
                0,
                tilemap.getWorldWidth(),
                -1,
                0,
                -1}, 0, 0, tilemapParams, 1);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(0);
        obj.setFriction(0);
        obj.setRestitution(0);
        obj.setName("floor");
        addObject(obj);

        // Add left wall
        obj = new PolygonObject(new float[]{-1,
                tilemap.getWorldHeight() * 1.5f,
                0,
                tilemap.getWorldHeight() * 1.5f,
                0,
                0,
                -1,
                0}, 0, 0, tilemapParams, 1);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(0);
        obj.setFriction(0);
        obj.setRestitution(0);
        obj.setName("leftwall");
        addObject(obj);

        // Add right wall
        obj = new PolygonObject(new float[]{tilemap.getWorldWidth(),
                tilemap.getWorldHeight() * 1.5f,
                tilemap.getWorldWidth() + 1,
                tilemap.getWorldHeight() * 1.5f,
                tilemap.getWorldWidth() + 1,
                0,
                tilemap.getWorldWidth(),
                0}, 0, 0, tilemapParams, 1);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(0);
        obj.setFriction(0);
        obj.setRestitution(0);
        obj.setName("rightwall");
        addObject(obj);

        world.setGravity(new Vector2(0, defaults.getFloat("gravity", 0)));
        avatar = new Player(constants.get("dude"),
                            0.5f,
                            tilemap.getTileHeight() * 0.9f,
                            jumpAnimator,
                            jogAnimator,
                            idleAnimator,
                            tilemapParams,
                            0.9f);

        avatar.setBoingSound(boingSound);
        avatar.setTexture(avatarTexture);
        avatar.setName("dude");
        addObject(avatar);
        collisionController = new CollisionController(this,
                                                      avatar,
                                                      uiController,
                                                      resourceController,
                                                      plantController,
                                                      hazardController);
        world.setContactListener(collisionController);
        collisionController.setSunSound(sunSound);
        collisionController.setWaterCollectSound(waterCollectSound);
        collisionController.setBugStompSound(bugStompSound);
    }

    public Texture getWaterIndicatorTexture() {
        return waterIndicatorTexture;
    }

}