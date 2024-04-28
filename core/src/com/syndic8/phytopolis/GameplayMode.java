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

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.*;
import com.syndic8.phytopolis.level.models.*;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

/**
 * Gameplay specific controller for the platformer game.
 * <p>
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class GameplayMode extends WorldController {

    private final Vector2 cameraVector;
    private final Vector2 projMousePosCache;
    private final InputController ic;
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
    private BitmapFont timesFont;
    private TextureRegion background;
    private TextureRegion vignette;
    private TextureRegion avatarTexture;
    private Tilemap tilemap;
    private Texture waterTexture;
    private float volume;
    private JsonValue constants;
    private Player avatar;
    private Music backgroundMusic;
    private String lvl;
    private CollisionController collisionController;

    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public GameplayMode() {
        super();
        cameraVector = new Vector2();
        projMousePosCache = new Vector2();
        gathered = false;
        ic = InputController.getInstance();
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
        tilemap = new Tilemap(DEFAULT_WIDTH,
                              DEFAULT_HEIGHT,
                              directory.getEntry(lvl, JsonValue.class));
        tilemap.gatherAssets(directory);
        if (!gathered) {
            gathered = true;
            avatarTexture = new TextureRegion(directory.getEntry(
                    "gameplay:player",
                    Texture.class));
            waterTexture = directory.getEntry("water_nooutline", Texture.class);
            timesFont = directory.getEntry("times", BitmapFont.class);
            background = new TextureRegion(directory.getEntry(
                    "gameplay:background",
                    Texture.class));
            vignette = new TextureRegion(directory.getEntry("ui:vignette",
                                                            Texture.class));
            background.setRegion(0, 0, 1920, 1080);
            vignette.setRegion(0, 0, 1920, 1080);

            jumpTexture = directory.getEntry("jump", Texture.class);
            jumpAnimator = new FilmStrip(jumpTexture, 1, 13, 13);

            jogTexture = directory.getEntry("jog", Texture.class);
            jogAnimator = new FilmStrip(jogTexture, 1, 8, 8);

            constants = directory.getEntry("gameplay:constants",
                                           JsonValue.class);

            resourceController = new ResourceController(canvas, tilemap);
            uiController = new UIController(canvas, tilemap);
            float branchHeight = tilemap.getTileHeight();
            float plantWidth = branchHeight * (float) Math.sqrt(3) * 4 / 2;
            float plantXOrigin = bounds.width / 2 - plantWidth / 2;
            plantController = new PlantController(5,
                                                  40,
                                                  tilemap.getTileHeight(),
                                                  plantXOrigin,
                                                  0,
                                                  world,
                                                  scale,
                                                  resourceController,
                                                  tilemap);
            hazardController = new HazardController(plantController,
                                                    (int) tilemap.getFireRate(),
                                                    1000000000,
                                                    6,
                                                    8,
                                                    6,
                                                    6,
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
            super.gatherAssets(directory);
            backgroundMusic = directory.getEntry("viridian", Music.class);
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0);
            backgroundMusic.play();
        }
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        super.show();
        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
        uiController.startTimer();
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

        backgroundMusic.setVolume(super.getVolume());
        // Process actions in object model
        avatar.setMovement(ic.getHorizontal() * avatar.getForce());
        avatar.setJumping(ic.didPrimary());
        processPlantGrowth();

        avatar.applyForce();

        if (ic.didScrollReset()) {
            ic.resetScrolled();
        }
        InputController.setHeight(
                tilemap.getTilemapHeight() - canvas.getHeight());
        cameraVector.set(canvas.getWidth() / 2f,
                         Math.max(canvas.getHeight() / 2f,
                                  Math.min(tilemap.getTilemapHeight() -
                                                   canvas.getHeight() / 2f,
                                           avatar.getY()) + ic.getScrolled()));
        // generate hazards please
        for (Model m : objects) {
            if (m instanceof Water) {
                ((Water) m).regenerate();
            }
            if (m instanceof Sun) {
                ((Sun) m).update(m.getY() < plantController.getMaxLeafHeight());

            }
            if (m instanceof Hazard){
                ((Hazard) m).update(dt);
            }
        }
        Sun s = sunController.spawnSuns(dt, tilemap);
        if (s != null) addObject(s);
        for (Hazard h : hazardController.updateHazards()) {
            addObject(h);
        }

        if (ic.didMousePress()) {
            projMousePosCache.set(ic.getGrowX(), ic.getGrowY());
            Vector2 unprojMousePos = canvas.unproject(projMousePosCache);
            hazardController.extinguishFire(unprojMousePos);
        }
        plantController.propagateDestruction();
        uiController.update(dt,
                            resourceController.getCurrRatio(),
                            hazardController);
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
        Vector2 unprojMousePos = canvas.unproject(projMousePosCache);

        if (ic.didMousePress()) {
            // process leaf stuff
            if (ic.didSpecial()) {
                // don't grow if there's a fire there (prioritize fire)
                if (!hazardController.hasFire(unprojMousePos)) {
                    Leaf.leafType lt = Leaf.leafType.NORMAL;
                    Model newLeaf = plantController.makeLeaf(unprojMousePos.x,
                                                             unprojMousePos.y +
                                                                     0.5f *
                                                                             tilemap.getTileHeight(),
                                                             lt);
                    if (newLeaf != null) addObject(newLeaf);
                }
            }

            // process branch stuff
            else {
                Branch branch = plantController.growBranch(unprojMousePos.x,
                                                           unprojMousePos.y);
                if (branch != null) addObject(branch);
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
     *
     * @param dt Number of seconds since last animation frame
     */
    public void draw(float dt) {
        canvas.clear();
        canvas.cameraUpdate(cameraVector);
        canvas.begin();
        drawBackground();

        tilemap.draw(canvas);

        super.draw(dt);

        if (!ic.didSpecial()) {
            projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
            Vector2 unprojMousePos = canvas.unproject(projMousePosCache);
            plantController.drawGhostBranch(canvas,
                                            unprojMousePos.x,
                                            unprojMousePos.y);
        }
        hazardController.draw(canvas);
        drawVignette();
        canvas.end();

        canvas.beginHud();
        hazardController.drawWarning(canvas, cameraVector);
        uiController.draw(canvas);
        canvas.endHud();
        super.draw(canvas);
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
        backgroundMusic.stop();
        backgroundMusic.play();
        uiController.reset(tilemap);

        ic.resetScrolled();

        resourceController.reset();
        plantController.reset();

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
                DEFAULT_WIDTH,
                0,
                DEFAULT_WIDTH,
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
                DEFAULT_HEIGHT,
                0,
                DEFAULT_HEIGHT,
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
        obj = new PolygonObject(new float[]{DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_WIDTH + 1,
                DEFAULT_HEIGHT,
                DEFAULT_WIDTH + 1,
                0,
                DEFAULT_WIDTH,
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
                                                      resourceController);
        world.setContactListener(collisionController);
        volume = constants.getFloat("volume", 1.0f);
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
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        super.hide();
        backgroundMusic.pause();
        uiController.pauseTimer();
    }

    private void drawBackground() {
        if (background != null) {
            canvas.draw(background.getTexture(),
                        Color.WHITE,
                        0,
                        0,
                        canvas.getWidth(),
                        canvas.getHeight() * 4);
        }
    }

    private void drawVignette() {
        float backgroundY = canvas.getCameraY() - canvas.getViewPortY() / 2;
        canvas.draw(vignette.getTexture(),
                    Color.WHITE,
                    0,
                    backgroundY,
                    canvas.getWidth(),
                    canvas.getHeight());
    }

}