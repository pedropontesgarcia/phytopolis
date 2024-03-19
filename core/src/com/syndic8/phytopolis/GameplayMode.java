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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.HazardController;
import com.syndic8.phytopolis.level.PlantController;
import com.syndic8.phytopolis.level.models.*;

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
    final short CATEGORY_PLAYER_FALL_THROUGH = 0x0004;
    // Define collision masks
    final short MASK_PLAYER = CATEGORY_PLATFORM;
    final short MASK_PLATFORM = CATEGORY_PLAYER;
    final short MASK_PLAYER_FALL_THROUGH = CATEGORY_PLATFORM;
    private final float distance = 120f;
    private final PlantController plantController;
    private final HazardController hazardController;
    private final long fireId = -1;
    private final long plopId = -1;
    private final long jumpId = -1;
    // Physics objects for the game
    private final Vector2 cameraVector;
    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;
    /**
     * branch texture
     */
    protected TextureRegion branchTexture;
    private Player player;
    private TextureRegion background;
    /**
     * Texture asset for character avatar
     */
    private TextureRegion avatarTexture;
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
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        cameraVector = new Vector2();
        sensorFixtures = new ObjectSet<Fixture>();
        plantController = new PlantController(13,
                                              13,
                                              1.4f,
                                              1,
                                              1.1f,
                                              world,
                                              scale);
        hazardController = new HazardController(plantController, 30, 30, 300, 200);

        background = null;


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
        avatarTexture = new TextureRegion(directory.getEntry("gameplay:player",
                                                             Texture.class));
        barrierTexture = new TextureRegion(directory.getEntry("gameplay:barrier",
                                                              Texture.class));
        background = new TextureRegion(directory.getEntry("gameplay:background",
                                                          Texture.class));
        background.setRegion(0, 0, 1920, 1080);
        super.setBackground(background.getTexture());
        plantController.gatherAssets(directory);
        hazardController.gatherAssets(directory);

        //jumpSound = directory.getEntry("platform:jump", Sound.class);
        //fireSound = directory.getEntry("platform:pew", Sound.class);
        //plopSound = directory.getEntry("platform:plop", Sound.class);

        constants = directory.getEntry("gameplay:constants", JsonValue.class);

        this.branchTexture = new TextureRegion(directory.getEntry(
                "gameplay:branch",
                Texture.class));
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
        // Add level goal
        float dwidth;
        float dheight;

        String wname = "wall";
        JsonValue walljv = constants.get("walls");
        JsonValue defaults = constants.get("defaults");
        for (int ii = 0; ii < walljv.size; ii++) {
            PolygonObject obj;
            obj = new PolygonObject(walljv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(barrierTexture);
            obj.setName(wname + ii);
            addObject(obj);
        }

        String pname = "platform";
        JsonValue platjv = constants.get("platforms");
        for (int ii = 0; ii < platjv.size; ii++) {
            PolygonObject obj;
            obj = new PolygonObject(platjv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(barrierTexture);
            obj.setName(pname + ii);
            addObject(obj);
        }

        // This world is heavier
        world.setGravity(new Vector2(0, defaults.getFloat("gravity", 0)));

        // Create dude
        dwidth = avatarTexture.getRegionWidth() / scale.x;
        dheight = avatarTexture.getRegionHeight() / scale.y;
        avatar = new Player(constants.get("dude"), dwidth, dheight);
        avatar.setDrawScale(scale);
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

    /**
     * Processes plant growth using player input. Grows a branch in the
     * corresponding direction at the node closest to the player's position.
     *
     * @return whether the plant grew.
     */
    public boolean processPlantGrowth() {
        // TODO: position branch correctly
        float avatarX = avatar.getX();
        float avatarY = avatar.getY();
        if (InputController.getInstance().didGrowUp() &&
                (plantController.canGrowAt(avatarX, avatarY))) {
            plantController.growBranch(avatarX,
                                       avatarY,
                                       PlantController.branchDirection.MIDDLE,
                                       PlantController.branchType.NORMAL);
        } else if (InputController.getInstance().didGrowRight() &&
                (plantController.canGrowAt(avatarX, avatarY))) {
            plantController.growBranch(avatarX,
                                       avatarY,
                                       PlantController.branchDirection.RIGHT,
                                       PlantController.branchType.NORMAL);
        } else if (InputController.getInstance().didGrowLeft() &&
                (plantController.canGrowAt(avatarX, avatarY))) {
            plantController.growBranch(avatarX,
                                       avatarY,
                                       PlantController.branchDirection.LEFT,
                                       PlantController.branchType.NORMAL);

        } else if (InputController.getInstance().didMousePress()) {
            plantController.growLeaf(
                    InputController.getInstance().getGrowX() / 120f,
                    InputController.getInstance().getGrowY() / 120f,
                    PlantController.leafType.NORMAL,
                    this);
            System.out.println("here");
            System.out.println(InputController.getInstance().getGrowY() / 120f);
            System.out.println(InputController.getInstance().getGrowX());
        }
        return false;
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
        //avatar.setShooting(InputController.getInstance().didSecondary());
        processPlantGrowth();

        avatar.applyForce();
        if (avatar.isJumping()) {
            //jumpId = playSound(jumpSound, jumpId, volume);
        }
        //        hazardController.updateHazards();

        //handleDrop();
        cameraVector.set(8 * 1920 / 16.0f,
                         (avatar.getY() + 3.5f) * 1080 / 9.0f);
        // generate hazards please
        hazardController.updateHazards();
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
                setComplete(true);
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
        super.draw(dt);
        canvas.begin();
        plantController.draw(canvas);
        player.draw(canvas);
        hazardController.draw(canvas);
        canvas.end();
    }

}