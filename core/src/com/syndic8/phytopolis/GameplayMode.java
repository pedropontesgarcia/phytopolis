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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.*;
import com.syndic8.phytopolis.level.models.*;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

import java.util.HashMap;

/**
 * Gameplay specific controller for the platformer game.
 * <p>
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class GameplayMode extends WorldController implements ContactListener {

    private final Vector2 cameraVector;
    private final Vector2 projMousePosCache;
    private final InputController ic = InputController.getInstance();
    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;
    protected Texture jumpTexture;
    private TextureRegion branchCursorTexture;
    private TextureRegion leafCursorTexture;
    private TextureRegion waterCursorTexture;
    private Cursor branchCursor;
    private Cursor leafCursor;
    private Cursor waterCursor;
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
    private Texture nodeTexture;
    private float volume;
    private JsonValue constants;
    private Player avatar;
    private HashMap<Fixture, Filter> originalCollisionProperties;
    private Music backgroundMusic;
    private String lvl;

    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public GameplayMode() {
        super();
        world.setContactListener(this);
        cameraVector = new Vector2();
        sensorFixtures = new ObjectSet<>();
        projMousePosCache = new Vector2();
        gathered = false;
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
        if (!gathered) {
            gathered = true;
            branchCursorTexture = new TextureRegion(directory.getEntry(
                    "ui:branch-cursor",
                    Texture.class));
            leafCursorTexture = new TextureRegion(directory.getEntry(
                    "ui:leaf-cursor",
                    Texture.class));
            waterCursorTexture = new TextureRegion(directory.getEntry(
                    "ui:water-cursor",
                    Texture.class));
            Pixmap pixmap = getPixmapFromRegion(branchCursorTexture);
            branchCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
            pixmap = getPixmapFromRegion(leafCursorTexture);
            leafCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
            pixmap = getPixmapFromRegion(waterCursorTexture);
            waterCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
            pixmap.dispose();

            avatarTexture = new TextureRegion(directory.getEntry("gameplay:player",
                    Texture.class));
            waterTexture = directory.getEntry("water_nooutline", Texture.class);
            timesFont = directory.getEntry("times", BitmapFont.class);
            background = new TextureRegion(directory.getEntry("gameplay:background",
                    Texture.class));
            vignette = new TextureRegion(directory.getEntry("gameplay:vignette",
                    Texture.class));

            background.setRegion(0, 0, 1920, 1080);
            vignette.setRegion(0, 0, 1920, 1080);

            jumpTexture = directory.getEntry("jump", Texture.class);
            jumpAnimator = new FilmStrip(jumpTexture, 1, 13, 13);

            jogTexture = directory.getEntry("jog", Texture.class);
            jogAnimator = new FilmStrip(jogTexture, 1, 8, 8);

            constants = directory.getEntry("gameplay:constants", JsonValue.class);
            tilemap = new Tilemap(DEFAULT_WIDTH,
                    DEFAULT_HEIGHT,
                    directory.getEntry(lvl, JsonValue.class));
            tilemap.gatherAssets(directory);

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
                    8,
                    6,
                    tilemap);
            sunController = new SunController(5,
                    10,
                    tilemap.getTileWidth() * 1.5f,
                    bounds.width -
                            tilemap.getTileWidth() * 1.5f,
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
                Sun s = (Sun) m;
                if (s.belowScreen() ||
                        s.getY() < plantController.getMaxLeafHeight() - 1) {
                    s.clear();
                }
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
        uiController.update(dt, resourceController.getCurrRatio());
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
                    Model newLeaf = plantController.upgradeLeaf(unprojMousePos.x,
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
        drawVignette();

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
        canvas.end();

        updateCursor();

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

        ic.resetScrolled();

        resourceController.reset();
        plantController.reset();

        world = new World(gravity, false);
        world.setContactListener(this);
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

        originalCollisionProperties = new HashMap<>();

        Array<Fixture> fixtures = avatar.getBody().getFixtureList();
        for (Fixture fixture : fixtures) {
            originalCollisionProperties.put(fixture, fixture.getFilterData());
        }

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

    /**
     * Updates the custom cursor
     */
    public void updateCursor() {
        Gdx.graphics.setCursor(branchCursor);
        if (ic.didSpecial()) {
            Gdx.graphics.setCursor(leafCursor);
        }
        projMousePosCache.set(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unproject(projMousePosCache);
        if (hazardController.hasFire(unprojMousePos)) {
            Gdx.graphics.setCursor(waterCursor);
        }
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

    //    /**
    //     * @param categoryBits the collision category for the
    //     *                     player character when falling through the platform
    //     *                     What  the player is.
    //     * @param maskBits     Categories the player can collide
    //     *                     with
    //     * @param collide      whether fixtures wit the same
    //     *                     category should collide or not
    //     * @return Filter that will allow the player to pass
    //     * through a platform without collision
    //     */
    //    private Filter createFilterData(short categoryBits,
    //                                    short maskBits,
    //                                    boolean collide) {
    //        Filter filter = new Filter();
    //        filter.categoryBits = categoryBits;
    //        filter.maskBits = maskBits;
    //        filter.groupIndex = 0; // Default group index, modify if necessary
    //        return filter;
    //    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Model bd1 = (Model) body1.getUserData();
            Model bd2 = (Model) body2.getUserData();

            // See if we have landed on the ground.
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1 &&
                    (bd1.getType() == Model.ModelType.LEAF ||
                            bd1.getType() == Model.ModelType.PLATFORM ||
                            bd1.getType() == Model.ModelType.TILE_FULL)) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2) &&
                            (bd2.getType() == Model.ModelType.LEAF ||
                                    bd2.getType() == Model.ModelType.PLATFORM ||
                                    bd2.getType() ==
                                            Model.ModelType.TILE_FULL)) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1 &&
                    bd1.getType() == Model.ModelType.LEAF) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2 &&
                        bd2.getType() == Model.ModelType.LEAF)) {
                Leaf l = (Leaf) (avatar == bd1 ? bd2 : bd1);
                if (l.getLeafType() == Leaf.leafType.BOUNCY) {
                    avatar.setBouncy(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the end of a collision
     * <p>
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        contact.setEnabled(true);
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }
        try {
            if (((Model) bd1).getType() == Model.ModelType.LEAF ||
                    ((Model) bd2).getType() == Model.ModelType.LEAF) {
                Leaf l = (Leaf) (avatar == bd1 ? bd2 : bd1);
                if (l.getLeafType() == Leaf.leafType.BOUNCY) {
                    avatar.setBouncy(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Unused ContactListener method
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();
        boolean isCollisionBetweenPlayerAndLeaf =
                (fix1.getBody() == avatar.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.LEAF) ||
                        (fix2.getBody() == avatar.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.LEAF);
        boolean isCollisionBetweenPlayerAndNoTopTile =
                (fix1.getBody() == avatar.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_NOTOP) ||
                        (fix2.getBody() == avatar.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.TILE_NOTOP);
        boolean isCollisionBetweenPlayerAndWater =
                (fix1.getBody() == avatar.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.WATER) ||
                        (fix2.getBody() == avatar.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.WATER);
        boolean isCollisionBetweenPlayerAndSun =
                (fix1.getBody() == avatar.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (fix2.getBody() == avatar.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN);
        boolean isCollisionBetweenLeafAndSun =
                (((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.LEAF &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.LEAF && ((Model) fix1.getBody()
                                .getUserData()).getType() ==
                                Model.ModelType.SUN);
        boolean isCollisionBetweenPlatformAndSun =
                (((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.PLATFORM &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.PLATFORM &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN);
        boolean isCollisionBetweenTileAndSun =
                (((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.TILE_NOTOP &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_NOTOP &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN) ||
                        (((Model) fix1.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_FULL &&
                                ((Model) fix2.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_FULL &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN);
        if (isCollisionBetweenPlayerAndSun ||
                isCollisionBetweenPlatformAndSun ||
                isCollisionBetweenTileAndSun) {
            contact.setEnabled(false);
        }
        if (isCollisionBetweenLeafAndSun) {
            Sun s;
            if (((Model) fix1.getBody().getUserData()).getType() ==
                    Model.ModelType.SUN) {
                s = (Sun) fix1.getBody().getUserData();
            } else {
                s = (Sun) fix2.getBody().getUserData();
            }
            contact.setEnabled(false);
            s.clear();
            uiController.addTime();
        }
        if (isCollisionBetweenPlayerAndWater) {
            Water w;
            if (((Model) fix1.getBody().getUserData()).getType() ==
                    Model.ModelType.WATER) {
                w = (Water) fix1.getBody().getUserData();
            } else {
                w = (Water) fix2.getBody().getUserData();
            }
            contact.setEnabled(false);
            if (w.isFull()) {
                w.clear();
                resourceController.pickupWater();
            }
        }

        boolean isPlayerGoingUp = avatar.getVY() >= 0;
        boolean isPlayerGoingDown = avatar.getVY() <= 0;
        boolean isPlayerBelow = false;
        if (fix1.getBody() == avatar.getBody()) isPlayerBelow =
                fix1.getBody().getPosition().y - avatar.getHeight() / 2f <
                        fix2.getBody().getPosition().y;
        else if (fix2.getBody() == avatar.getBody()) isPlayerBelow =
                fix2.getBody().getPosition().y - avatar.getHeight() / 2f <
                        fix1.getBody().getPosition().y;
        if (isCollisionBetweenPlayerAndLeaf &&
                (isPlayerGoingUp || isPlayerBelow ||
                        ic.didDrop())) {
            contact.setEnabled(false);
        }
        if (isCollisionBetweenPlayerAndNoTopTile && isPlayerGoingDown) {
            contact.setEnabled(false);
        }
//        if (isCollisionBetweenPlayerAndLeaf) {
//            Leaf l;
//            if (fix1.getBody() == avatar.getBody())
//                l = (Leaf) fix2.getBody().getUserData();
//            else l = (Leaf) fix1.getBody().getUserData();
//            if (l.getLeafType() == Leaf.leafType.BOUNCY &&
//                    avatar.getY() > l.getY()) avatar.setBouncy(true);
//        }
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

}