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
import com.badlogic.gdx.audio.Sound;
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
import com.syndic8.phytopolis.level.HazardController;
import com.syndic8.phytopolis.level.PlantController;
import com.syndic8.phytopolis.level.ResourceController;
import com.syndic8.phytopolis.level.models.*;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;
import com.syndic8.phytopolis.util.Timer;

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

    // Define collision categories (bits)
    final short CATEGORY_PLAYER = 0x0001;
    final short CATEGORY_PLATFORM = 0x0002;
    private final Vector2 cameraVector;
    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;
    protected Texture jumpTexture;
    private TextureRegion branchCursorTexture;
    private Timer timer;
    private int starPoints;
    private TextureRegion leafCursorTexture;
    private TextureRegion waterCursorTexture;
    private Cursor branchCursor;
    private Cursor leafCursor;
    private Cursor waterCursor;
    private PlantController plantController;
    private HazardController hazardController;
    private ResourceController resourceController;
    private FilmStrip jumpAnimator;

    private Texture jogTexture;
    private FilmStrip jogAnimator;
    private int sunCollected;
    private int waterCollected;
    private Player player;
    /**
     * The font for giving messages to the player
     */
    private BitmapFont timesFont;
    private TextureRegion background;
    private TextureRegion vignette;
    /**
     * Texture asset for character avatar
     */
    private TextureRegion avatarTexture;
    private Tilemap tilemap;
    /**
     * Texture asset for water symbol
     */
    private Texture waterTexture;
    /**
     * Texture asset for the spinning barrier
     */
    private TextureRegion barrierTexture;
    /**
     * Texture asset for the bullet
     */
    private TextureRegion bulletTexture;
    /**
     * Texture asset for the bridge plank
     */
    private TextureRegion bridgeTexture;
    /**
     * Texture asset for a node
     */
    private Texture nodeTexture;
    /**
     * The jump sound.  We only want to play once.
     */
    private Sound jumpSound;
    /**
     * The weapon fire sound.  We only want to play once.
     */
    private Sound fireSound;
    /**
     * The weapon pop sound.  We only want to play once.
     */
    private Sound plopSound;
    /**
     * The default sound volume
     */
    private float volume;
    /**
     * Physics constants for initialization
     */
    private JsonValue constants;
    /**
     * Reference to the character avatar
     */
    private Player avatar;
    /**
     * Reference to the goalDoor (for collision detection)
     */
    private BoxObject goalDoor;
    private boolean fall;
    private float startHeight;
    private HashMap<Fixture, Filter> originalCollisionProperties;
    private Music backgroundMusic;

    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public GameplayMode() {
        super();
        world.setContactListener(this);
        cameraVector = new Vector2();
        sensorFixtures = new ObjectSet<Fixture>();
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
        branchCursor = Gdx.graphics.newCursor(pixmap, 64 / 2, 64 / 2);
        pixmap = getPixmapFromRegion(leafCursorTexture);
        leafCursor = Gdx.graphics.newCursor(pixmap, 64 / 2, 64 / 2);
        pixmap = getPixmapFromRegion(waterCursorTexture);
        waterCursor = Gdx.graphics.newCursor(pixmap, 64 / 2, 64 / 2);
        pixmap.dispose();

        avatarTexture = new TextureRegion(directory.getEntry("gameplay:player",
                                                             Texture.class));
        barrierTexture = new TextureRegion(directory.getEntry("gameplay:barrier",
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

        //jumpSound = directory.getEntry("platform:jump", Sound.class);
        //fireSound = directory.getEntry("platform:pew", Sound.class);
        //plopSound = directory.getEntry("platform:plop", Sound.class);

        constants = directory.getEntry("gameplay:constants", JsonValue.class);
        tilemap = new Tilemap(DEFAULT_WIDTH,
                              DEFAULT_HEIGHT,
                              directory.getEntry("gameplay:tilemap",
                                                 JsonValue.class));
        tilemap.gatherAssets(directory);

        //        this.branchTexture = new FilmStrip(directory.getEntry(
        //                "gameplay:branch",
        //                Texture.class), 1, 5, 5);

        resourceController = new ResourceController();
        plantController = new PlantController(8,
                                              40,
                                              tilemap.getTileHeight(),
                                              tilemap.getTileWidth(),
                                              0,
                                              world,
                                              scale,
                                              resourceController,
                                              tilemap);
        hazardController = new HazardController(plantController,
                                                8,
                                                1000000000,
                                                8,
                                                6,
                                                tilemap);
        plantController.gatherAssets(directory);
        hazardController.gatherAssets(directory);
        resourceController.gatherAssets(directory);
        super.gatherAssets(directory);
        backgroundMusic = directory.getEntry("viridian", Music.class);
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
    }

    /**
     *
     */
    private void setPlayer(Player player) {
        this.player = player;
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
        setPlayer(avatar);

        originalCollisionProperties = new HashMap<>();

        Array<Fixture> fixtures = avatar.getBody().getFixtureList();
        for (Fixture fixture : fixtures) {
            originalCollisionProperties.put(fixture, fixture.getFilterData());
        }

        volume = constants.getFloat("volume", 1.0f);
        timer = new Timer(300, 3, 100);
        timer.startTimer();
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

        if (!isFailure() && avatar.getY() < -1) {
            setFailure(true);
            return false;
        }

        return true;
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
     * Updates the custom cursor
     */
    public void updateCursor() {
        Gdx.graphics.setCursor(branchCursor);
        if (InputController.getInstance().didSpecial()) {
            Gdx.graphics.setCursor(leafCursor);
        }
        InputController ic = InputController.getInstance();
        Vector2 projMousePos = new Vector2(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unproject(projMousePos);
        if (hazardController.hasFire(unprojMousePos)) {
            Gdx.graphics.setCursor(waterCursor);
        }
    }

    /**
     * Processes plant growth using player input. Grows a branch in the
     * corresponding direction at the node closest to the player's position.
     */
    public void processPlantGrowth() {
        // get mouse position
        InputController ic = InputController.getInstance();
        Vector2 projMousePos = new Vector2(ic.getMouseX(), ic.getMouseY());
        Vector2 unprojMousePos = canvas.unproject(projMousePos);

        // draw ghost branches
        //        Branch hoveringBranch = plantController.screenToBranch(unprojMousePos.x, unprojMousePos.y);
        //        if (hoveringBranch != null && !objects.contains(hoveringBranch)) objects.add(hoveringBranch);

        if (InputController.getInstance().didMousePress()) {
            // process leaf stuff
            if (InputController.getInstance().didSpecial()) {
                // don't grow if there's a fire there (prioritize fire)
                if (!hazardController.hasFire(unprojMousePos)) {
                    Leaf.leafType lt = Leaf.leafType.NORMAL;
                    //            if (InputController.getInstance().didSpecial())
                    //                lt = Leaf.leafType.BOUNCY;
                    Model newLeaf = plantController.growLeaf(unprojMousePos.x,
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
        // Process actions in object model
        avatar.setMovement(InputController.getInstance().getHorizontal() *
                                   avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        //Leaf.leafType ltype = plantController.getLeafType(plantController.xWorldCoordToIndex(avatar.getX()), plantController.yWorldCoordToIndex(avatar.getY()));
        //avatar.setBouncy(ltype == Leaf.leafType.BOUNCY);

        //avatar.setShooting(InputController.getInstance().didSecondary());
        processPlantGrowth();

        avatar.applyForce();
        avatar.setBouncy(false);
        //System.out.println(avatar.getY());
        //System.out.println(avatar.atBottom());
        //        if (avatar.isJumping()) {
        //jumpId = playSound(jumpSound, jumpId, volume);
        //        }

        //handleDrop();
        cameraVector.set(8,
                         Math.max(avatar.getY() - canvas.getHeight() / 6f,
                                  canvas.getHeight() / 2f));
        // generate hazards please
        for (Model m : objects) {
            if (m instanceof Water) {
                ((Water) m).regenerate();
            }
        }
        hazardController.updateHazards();
        InputController ic = InputController.getInstance();
        if (ic.didMousePress()) {
            Vector2 projMousePos = new Vector2(ic.getGrowX(), ic.getGrowY());
            Vector2 unprojMousePos = canvas.unproject(projMousePos);
            hazardController.extinguishFire(unprojMousePos);
        }
        plantController.propagateDestruction();
        //        System.out.println(objects.size());
        timer.updateTime();
    }

    //    /**
    //     * Handles the drop mechanic when the player has
    //     * pressed S
    //     */
    //    private void handleDrop() {
    //        if (avatar.isPlayerOnPlatform(world) &&
    //                InputController.getInstance().dropped()) {
    //            for (Fixture fixture : avatar.getBody().getFixtureList()) {
    //                originalCollisionProperties.put(fixture,
    //                        fixture.getFilterData());
    //            }
    //            fall = true;
    //        }
    //        if (fall) {
    //            for (Fixture fixture : avatar.getBody().getFixtureList()) {
    //                fixture.setFilterData(createFilterData(
    //                        CATEGORY_PLAYER_FALL_THROUGH,
    //                        MASK_PLAYER_FALL_THROUGH,
    //                        false));
    //                fixture.setSensor(true);
    //                startHeight = avatar.getY();
    //            }
    //            fall = false;
    //        }
    //        if ((avatar.getY() <= startHeight - distance)) {
    //            Array<Fixture> fixtures = avatar.getBody().getFixtureList();
    //            for (Fixture fixture : fixtures) {
    //                Filter originalProperties = originalCollisionProperties.get(
    //                        fixture);
    //                if (originalProperties != null) {
    //                    fixture.setFilterData(originalProperties);
    //                    //                    fixture.setSensor(false);
    //                }
    //            }
    //            originalCollisionProperties.clear();
    //
    //            startHeight = 0;
    //        }
    //    }

    /**
     * @param categoryBits the collision category for the
     *                     player character when falling through the platform
     *                     What  the player is.
     * @param maskBits     Categories the player can collide
     *                     with
     * @param collide      whether fixtures wit the same
     *                     category should collide or not
     * @return Filter that will allow the player to pass
     * through a platform without collision
     */
    private Filter createFilterData(short categoryBits,
                                    short maskBits,
                                    boolean collide) {
        Filter filter = new Filter();
        filter.categoryBits = categoryBits;
        filter.maskBits = maskBits;
        filter.groupIndex = 0; // Default group index, modify if necessary
        return filter;
    }

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
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ?
                                           fix2 :
                                           fix1); // Could have more than one ground
            }

            // Check for win condition
            if ((bd1 == avatar && bd1.getY() > 34)) {
                timer.setRunning(false);
                starPoints = timer.getAcquiredStars();
                setComplete(true);

            }
            //            //Check for bouncyness
            //            if (bd1 == avatar && bd2 instanceof Leaf) {
            //                Leaf l1 = (Leaf) bd2;
            //                if(l1.getLeafType() == Leaf.leafType.BOUNCY &&
            //                        bd1.getY() > bd2.getY() + 0.9f) avatar.setBouncy(true);
            //            }

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
        //        if (bd1 == avatar && bd2 instanceof Leaf) {
        //            avatar.setBouncy(false);
        //        }
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /**
     * Unused ContactListener method
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
        sunCollected = 0;
        waterCollected = 0;
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
        if (isCollisionBetweenPlayerAndSun) {
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
            s.clear();
            contact.setEnabled(false);
//            if (isCollisionBetweenPlayerAndSun) {
            resourceController.pickupSun();
//            }
        }
        if (isCollisionBetweenPlayerAndWater) {
            Water w;
            if (((Model) fix1.getBody().getUserData()).getType() ==
                    Model.ModelType.WATER) {
                w = (Water) fix1.getBody().getUserData();
            } else {
                w = (Water) fix2.getBody().getUserData();
            }
            w.clear();
            contact.setEnabled(false);
            resourceController.pickupWater(0.1f);
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
                        InputController.getInstance().didDrop())) {
            contact.setEnabled(false);
        }
        if (isCollisionBetweenPlayerAndNoTopTile && isPlayerGoingDown) {
            contact.setEnabled(false);
        }
        if (isCollisionBetweenPlayerAndLeaf) {
            Leaf l;
            if (fix1.getBody() == avatar.getBody())
                l = (Leaf) fix2.getBody().getUserData();
            else l = (Leaf) fix1.getBody().getUserData();
            if (l.getLeafType() == Leaf.leafType.BOUNCY &&
                    avatar.getY() > l.getY() + 0.5f) avatar.setBouncy(true);
        }
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
        backgroundMusic.stop();
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        super.show();
        backgroundMusic.play();
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

    private void drawVignette(){
        float base = Math.max(avatar.getY() - canvas.getHeight() / 6f,
                canvas.getHeight() / 2f);
        canvas.draw(vignette.getTexture(),
                Color.WHITE,
                0,
                base,
                canvas.getWidth(),
                canvas.getHeight());
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
        canvas.cameraUpdate(cameraVector);
        canvas.clear();
        canvas.begin();
        drawBackground();
        drawVignette();
        //timer.displayTime(canvas,timesFont, Color.BLACK, canvas.getWidth()/2, canvas.getHeight()/2, new Vector2(0.036f, 0.036f));
        //System.out.println(timer.getMinutes());
        //System.out.println(timer.getSeconds());

        tilemap.draw(canvas);

        super.draw(dt);

        //plantController.draw(canvas);
        InputController ic = InputController.getInstance();
        if (!ic.didSpecial()) {
            Vector2 projMousePos = new Vector2(ic.getMouseX(), ic.getMouseY());
            Vector2 unprojMousePos = canvas.unproject(projMousePos);
            plantController.drawGhostBranch(canvas,
                                            unprojMousePos.x,
                                            unprojMousePos.y);
        }
        hazardController.draw(canvas);
        //player.draw(canvas);
        canvas.end();

        updateCursor();

        canvas.beginHud();
        hazardController.drawWarning(canvas, cameraVector);
        resourceController.draw(canvas);
        canvas.endHud();

        canvas.beginText();
        timer.displayTime(canvas,
                          timesFont,
                          Color.WHITE,
                          Gdx.graphics.getWidth() / 2.1f,
                          Gdx.graphics.getHeight() / 1.03f,
                          new Vector2(Gdx.graphics.getWidth()/1129.412f, Gdx.graphics.getHeight()/635.294f));
        //canvas.drawTime(timesFont,"me", Color.WHITE, 800, 200);
        canvas.endtext();

    }

}