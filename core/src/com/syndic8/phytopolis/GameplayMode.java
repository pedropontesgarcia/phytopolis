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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.*;
import com.syndic8.phytopolis.level.models.*;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;
import edu.cornell.gdiac.audio.AudioSource;

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
    protected Texture jumpTexture;
    private PlantController plantController;
    private HazardController hazardController;
    private ResourceController resourceController;
    private SunController sunController;
    private UIController uiController;
    private FilmStrip jumpAnimator;
    private Texture jogTexture;
    private FilmStrip jogAnimator;
    private boolean gathered;
    private Texture background;
    private Texture avatarTexture;
    private Tilemap tilemap;
    private JsonValue constants;
    private Player avatar;
    private int backgroundMusic;
    private String lvl;
    private CollisionController collisionController;
    private float timeSinceUIUpdate = 0;

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
    }

    public void setLevel(String lvl) {
        this.lvl = lvl;
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
        tilemap = new Tilemap(directory.getEntry(lvl, JsonValue.class), canvas);
        tilemap.gatherAssets(directory);
        canvas.setWorldSize(tilemap.getWorldWidth());

        setBounds(tilemap.getWorldWidth(), tilemap.getWorldHeight());

        AudioSource bgm = directory.getEntry("viridian", AudioSource.class);
        backgroundMusic = soundController.addMusic(bgm);
        soundController.setMusic(backgroundMusic);
        soundController.setLooping(true);
        soundController.playMusic();

        if (!gathered) {
            gathered = true;
            avatarTexture = directory.getEntry("gameplay:player",
                                               Texture.class);
            background = directory.getEntry("gameplay:background",
                                            Texture.class);
            jumpTexture = directory.getEntry("jump", Texture.class);
            jumpAnimator = new FilmStrip(jumpTexture, 1, 13, 13);

            jogTexture = directory.getEntry("jog", Texture.class);
            jogAnimator = new FilmStrip(jogTexture, 1, 8, 8);

            constants = directory.getEntry("gameplay:constants",
                                           JsonValue.class);

            resourceController = new ResourceController();
            uiController = new UIController(canvas, tilemap);
            float branchHeight = tilemap.getTileHeight();
            int plantNodesPerRow = Math.round(
                    (tilemap.getTilemapWidth() - 2) * (float) Math.sqrt(3));
            float plantWidth = branchHeight * (float) Math.sqrt(3) *
                    (plantNodesPerRow - 1) / 2;
            float plantXOrigin = bounds.width / 2 - plantWidth / 2;
            plantController = new PlantController(plantNodesPerRow,
                                                  40,
                                                  tilemap.getTileHeight(),
                                                  plantXOrigin,
                                                  0,
                                                  world,
                                                  resourceController,
                                                  tilemap);
            hazardController = new HazardController(plantController,
                                                    (int) tilemap.getFireRate(),
                                                    1000000000,
                                                    6,
                                                    8,
                                                    6,
                                                    10,
                                                    tilemap.getPowerlineYVals(),
                                                    // REPLACE THIS!!
                                                    tilemap);
            sunController = new SunController(5,
                                              10,
                                              tilemap.getTileWidth() * 1.5f,
                                              bounds.width -
                                                      tilemap.getTileWidth() *
                                                              1.5f,
                                              bounds.height);
            plantController.gatherAssets(directory);
            hazardController.gatherAssets(directory);
            uiController.gatherAssets(directory);
            sunController.gatherAssets(directory);
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

        if (!isFailure() && uiController.timerDone()) {
            setFailure(true);
            fadeOut(1.5f);
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
        int water = resourceController.getCurrWater();
        hazardController.update(dt);
        soundController.setMusicVolume(super.getVolume());
        //        System.out.println("Music playing? " + soundController.isMusicPlaying());
        //        System.out.println("What music? " + soundController.getPlayingMusic());
        //        System.out.println("Is it looping? " + soundController.getIsLooping());
        // Process actions in object model
        avatar.setMovement(ic.getHorizontal() * avatar.getForce());
        avatar.setJumping(ic.didJump());
        processPlantGrowth();

        avatar.applyForce();

        if (ic.didScrollReset()) {
            ic.resetScrolled();
        }
        ic.setHeight(tilemap.getTilemapHeight() - canvas.getHeight());
        float aspectRatio = canvas.getWidth() / canvas.getHeight();
        float cameraHeight = tilemap.getWorldWidth() / aspectRatio;
        cameraVector.set(tilemap.getWorldWidth() / 2f,
                         Math.max(cameraHeight / 2f,
                                  Math.min(tilemap.getTilemapHeight() *
                                                   tilemap.getTileHeight() -
                                                   cameraHeight / 2f,
                                           avatar.getY()) + ic.getScrolled()));
        // generate hazards please
        for (Model m : objects) {
            if (m instanceof Water) {
                ((Water) m).regenerate();
            }
            if (m instanceof Sun) {
                ((Sun) m).update(dt, m.getY() < plantController.getMaxLeafHeight() -
                        resourceController.SUN_TOLERANCE);

            }
            if (m instanceof Hazard) {
                m.update(dt);
            }
        }
        Sun s = sunController.spawnSuns(dt, tilemap);
        if (s != null) addObject(s);
        for (Hazard h : hazardController.updateHazards()) {
            addObject(h);
        }

        if (ic.didMousePress()) {
            projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
            Vector2 unprojMousePos = canvas.unprojectGame(projMousePosCache);
            hazardController.extinguishFire(unprojMousePos);
        }
        plantController.propagateDestruction();

        if (timeSinceUIUpdate >= 1) {
            uiController.update(dt,
                                resourceController.getCurrRatio(),
                                hazardController,
                                collisionController.getAddedWater(),
                                resourceController.getCurrWater() < water,
                                plantController.countTimerDeductions(),
                                hazardController.getFireProgress());
            collisionController.setAddedWater(false);
        } else {
            timeSinceUIUpdate += 0.05;
        }
        // Check for win condition
        if ((plantController.getMaxLeafHeight() >
                tilemap.getVictoryHeight() * tilemap.getTileHeight()) &&
                !isComplete()) {
            setComplete(true);
            fadeOut(3);
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

                Branch branch = plantController.growBranch(unprojMousePos.x,
                                                           unprojMousePos.y);
                if (branch != null) addObject(branch);

            }
            if (shouldGrowLeaf) {
                // don't grow if there's a fire there (prioritize fire)

                Leaf.leafType lt = Leaf.leafType.NORMAL;
                float width = 0;
                switch (lvl) {
                    case "gameplay:lvl1":
                        lt = Leaf.leafType.NORMAL;
                        width = lvl1LeafWidth;
                        break;
                    case "gameplay:lvl2":
                        lt = Leaf.leafType.NORMAL1;
                        width = lvl2LeafWidth;
                        break;
                    case "gameplay:lvl3":
                        lt = Leaf.leafType.NORMAL2;
                        width = lvl3LeafWidth;
                        break;
                }
                Model newLeaf = plantController.makeLeaf(unprojMousePos.x,
                                                         unprojMousePos.y +
                                                                 0.5f *
                                                                         tilemap.getTileHeight(),
                                                         lt,
                                                         width);
                if (newLeaf != null) addObject(newLeaf);

            }
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
        canvas.cameraUpdate(cameraVector);
        canvas.beginGame();
        drawBackground();
        tilemap.draw(canvas);
        super.draw();
        if (!isPaused()) {
            projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
            Vector2 unprojMousePos = canvas.unprojectGame(projMousePosCache);
            if (!hazardController.hasFire(unprojMousePos)) {
                if (!ic.isGrowLeafModDown()) {
                    plantController.drawGhostBranch(canvas,
                                                    unprojMousePos.x,
                                                    unprojMousePos.y);
                } else {
                    Leaf.leafType lt;
                    float width;
                    switch (lvl) {
                        case "gameplay:lvl2":
                            lt = Leaf.leafType.NORMAL1;
                            width = lvl2LeafWidth;
                            break;
                        case "gameplay:lvl3":
                            lt = Leaf.leafType.NORMAL2;
                            width = lvl3LeafWidth;
                            break;
                        default:
                            lt = Leaf.leafType.NORMAL;
                            width = lvl1LeafWidth;
                    }
                    plantController.drawGhostLeaf(canvas,
                                                  lt,
                                                  width,
                                                  unprojMousePos.x,
                                                  unprojMousePos.y + 0.5f *
                                                          tilemap.getTileHeight());
                }

            }

        }
        hazardController.drawWarning(canvas, cameraVector);
        canvas.end();
        canvas.beginHud();
        uiController.draw(canvas);
        canvas.endHud();
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

    private void drawBackground() {
        if (background != null) {
            canvas.draw(background,
                        Color.WHITE,
                        -0.1f,
                        -0.1f,
                        tilemap.getWorldWidth(),
                        tilemap.getWorldWidth() * background.getHeight() /
                                background.getWidth());
        }
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
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
        uiController.reset();

        ic.resetScrolled();

        resourceController.reset();

        soundController.rewindMusic();

        float branchHeight = tilemap.getTileHeight();
        int plantNodesPerRow = Math.round(
                (tilemap.getTilemapWidth() - 2) * (float) Math.sqrt(3));
        float plantWidth =
                branchHeight * (float) Math.sqrt(3) * (plantNodesPerRow - 1) /
                        2;
        float plantXOrigin = bounds.width / 2 - plantWidth / 2;
        plantController.reset(plantNodesPerRow,
                              40,
                              tilemap.getTileHeight(),
                              plantXOrigin,
                              0,
                              tilemap);

        hazardController.reset((int) tilemap.getFireRate(),
                               1000000000,
                               6,
                               8,
                               6,
                               10,
                               tilemap);
        world = new World(gravity, false);
        setComplete(false);
        setFailure(false);
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        tilemap.populateLevel(this);
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
                -1}, 0, 0, tilemap, 1);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(0);
        obj.setFriction(0);
        obj.setRestitution(0);
        obj.setName("floor");
        addObject(obj);

        // Add left wall
        obj = new PolygonObject(new float[]{-1,
                tilemap.getWorldHeight(),
                0,
                tilemap.getWorldHeight(),
                0,
                0,
                -1,
                0}, 0, 0, tilemap, 1);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(0);
        obj.setFriction(0);
        obj.setRestitution(0);
        obj.setName("leftwall");
        addObject(obj);

        // Add right wall
        obj = new PolygonObject(new float[]{tilemap.getWorldWidth(),
                tilemap.getWorldHeight(),
                tilemap.getWorldWidth() + 1,
                tilemap.getWorldHeight(),
                tilemap.getWorldWidth() + 1,
                0,
                tilemap.getWorldWidth(),
                0}, 0, 0, tilemap, 1);
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
                            tilemap,
                            0.9f);
        avatar.setTexture(avatarTexture);
        avatar.setName("dude");
        addObject(avatar);
        collisionController = new CollisionController(avatar,
                                                      uiController,
                                                      resourceController,
                                                      plantController,
                                                      hazardController);
        world.setContactListener(collisionController);
    }

}