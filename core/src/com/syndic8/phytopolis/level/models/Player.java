package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.SoundController;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Player extends CapsuleObject {

    private static final int NUM_JOG_FRAMES = 8;
    private static final int NUM_JUMP_UP_FRAMES = 6;
    private static final int NUM_JUMP_DOWN_FRAMES = 6;
    private static final float ANIMATION_SPEED = 10f;
    private static final float ANIMATION_SPEED2 = 10f;

    private static final float ANIMATION_SPEED3 = 2.5f;
    /**
     * The initializing data (to avoid magic numbers)
     */
    private final JsonValue data;
    /**
     * The factor to multiply by the input
     */
    private final float force;
    /**
     * The amount to slow the character down
     */
    private final float damping;
    /**
     * The maximum character speed
     */
    private final float maxspeed;
    /**
     * Identifier to allow us to track the sensor in ContactListener
     */
    private final String sensorName;
    /**
     * The impulse for the character jump
     */
    private final float jump_force;
    /**
     * Cooldown (in animation frames) for jumping
     */
    private final int jumpLimit;
    /**
     * Cache for internal force calculations
     */
    private final Vector2 forceCache = new Vector2();
    private final FilmStrip jumpAnimator;
    private final FilmStrip jogAnimator;

    private final FilmStrip idleAnimator;
    /**
     * Multiplier for when standing on bouncy leaf
     */
    private final float bouncyMultiplier = 2f;
    private final int bouncyTimerMax = 7;
    /**
     * How long until we can shoot again
     */
    private final int shootCooldown;
    private float animFrame;
    private float animFrame2;

    private float animFrame3;
    /**
     * The current horizontal movement of the character
     */
    private float movement;
    /**
     * Which direction is the character facing
     */
    private boolean faceRight;
    /**
     * How long until we can jump again
     */
    private int jumpCooldown;
    /**
     * Whether we are actively jumping
     */
    private boolean isJumping;
    /**
     * Whether our feet are on the ground
     */
    private boolean isGrounded;
    /**
     * The physics shape of this object
     */
    private PolygonShape sensorShape;
    /**
     * whether the dude can jump extra high
     */
    private boolean bouncy = false;
    private int bouncyTimer = 0;
    private int boingSound;

    /**
     * Creates a new dude avatar with the given physics data
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data   The physics constants for this dude
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public Player(JsonValue data,
                  float width,
                  float height,
                  FilmStrip jump,
                  FilmStrip jog,
                  FilmStrip idle,
                  Tilemap.TilemapParams tmp,
                  float texScl) {
        // The shrink factors fit the image to a tigher hitbox
        super(data.get("pos").getFloat(0),
              data.get("pos").getFloat(1),
              width * data.get("shrink").getFloat(0),
              height * data.get("shrink").getFloat(1),
              tmp,
              texScl);
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction",
                                  0));  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);
        jump_force = data.getFloat("jump_force", 0);
        jumpLimit = data.getInt("jump_cool", 0);
        sensorName = "DudeGroundSensor";
        this.data = data;

        // Gameplay attributes
        isGrounded = false;
        isJumping = false;
        faceRight = true;

        animFrame = 0.0f;
        animFrame3 = 0.0f;
        jumpAnimator = jump;
        jogAnimator = jog;
        idleAnimator = idle;
        shootCooldown = 0;
        jumpCooldown = 0;
        setName("dude");

        zIndex = 5;
    }

    /**
     * sets whether the dude can jump extra high, for example, if the dude is standing on a bouncy leaf
     *
     * @param value whether the dude can jump extra high
     */
    public void setBouncy(boolean value) {
        bouncy = value;
        if (value) {
            bouncyTimer = bouncyTimerMax;
        }
    }

    public boolean getBounce() {
        return bouncyTimer > 0;
    }

    /**
     * Returns how much force to apply to get the dude moving
     * <p>
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     * <p>
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        if (!isGrounded()) {
            if (getVY() > 0.1) {
                if (animFrame < NUM_JUMP_UP_FRAMES) {
                    animFrame += dt * ANIMATION_SPEED;
                }
                if (animFrame >= NUM_JUMP_UP_FRAMES) {
                    animFrame = NUM_JUMP_UP_FRAMES - 1;
                }
                //                animFrame %= NUM_JUMP_UP_FRAMES;
                //            if (animFrame >= NUM_JUMP_FRAMES) {
                //                animFrame -= NUM_JUMP_FRAMES;
                //            }
            } else if (getVY() < -0.1) {
                if (animFrame < NUM_JUMP_UP_FRAMES) {
                    animFrame = NUM_JUMP_UP_FRAMES;
                } else if (animFrame <
                        NUM_JUMP_UP_FRAMES + NUM_JUMP_DOWN_FRAMES) {
                    animFrame += dt * ANIMATION_SPEED;
                    //                    animFrame = ((animFrame - NUM_JUMP_UP_FRAMES) % NUM_JUMP_DOWN_FRAMES) + NUM_JUMP_UP_FRAMES;
                }
                if (animFrame >= NUM_JUMP_UP_FRAMES + NUM_JUMP_DOWN_FRAMES) {
                    animFrame = NUM_JUMP_UP_FRAMES + NUM_JUMP_DOWN_FRAMES - 1;
                }
            }
        } else if (animFrame != 0) {
            if (animFrame < 11) {
                animFrame = 11;
            } else {
                animFrame += dt * ANIMATION_SPEED;
            }
            if (animFrame >= jumpAnimator.getSize()) {
                animFrame = 0;
            }
        }

        if (Math.abs(getVX()) >= 0.1) {
            animFrame2 += dt * ANIMATION_SPEED2;
            animFrame2 %= NUM_JOG_FRAMES;
            //            if (animFrame2 >= NUM_JOG_FRAMES) {
            //                animFrame2 -= NUM_JOG_FRAMES;
            //            }

        } else {
            animFrame2 = 0;
        }
        if (!bouncy) bouncyTimer--;

        // Apply cooldowns
        if (isJumping()) {
            jumpCooldown = jumpLimit;

        } else {
            jumpCooldown = Math.max(0, jumpCooldown - 1);
        }

        if (!(Math.abs(getVX()) >= 0.1) && !(!isGrounded() || animFrame != 0)) {
            animFrame3 += dt * ANIMATION_SPEED3;
            animFrame3 %= 4;

        }

        super.update(dt);
    }

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded && Math.abs(getVY()) < 0.2;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }

    /**
     * Returns true if the dude is actively jumping.
     *
     * @return true if the dude is actively jumping.
     */
    public boolean isJumping() {
        return isJumping && isGrounded && jumpCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively jumping.
     *
     * @param value whether the dude is actively jumping.
     */
    public void setJumping(boolean value) {
        isJumping = value;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;

        float width = tilemapParams.tileWidth() * textureSclInTiles;
        float height = tilemapParams.tileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();

        if (!isGrounded() || animFrame != 0) {
            jumpAnimator.setFrame((int) animFrame);
            float x = jumpAnimator.getRegionWidth() / 2.0f;
            float y = jumpAnimator.getRegionHeight() / 2.0f;
            canvas.draw(jumpAnimator,
                        Color.WHITE,
                        x,
                        y,
                        getX(),
                        getY(),
                        getAngle(),
                        sclX * effect,
                        sclY);

        } else if (Math.abs(getVX()) >= 0.1) {
            jogAnimator.setFrame((int) animFrame2);
            float x = jogAnimator.getRegionWidth() / 2.0f;
            float y = jogAnimator.getRegionHeight() / 2.0f;
            canvas.draw(jogAnimator,
                        Color.WHITE,
                        x,
                        y,
                        getX(),
                        getY(),
                        getAngle(),
                        sclX * effect,
                        sclY);
        } else {
            idleAnimator.setFrame((int) (animFrame3 >= 3 ? 1 : animFrame3));
            float x = idleAnimator.getRegionWidth() / 2.0f;
            float y = idleAnimator.getRegionHeight() / 2.0f;
            canvas.draw(idleAnimator,
                        Color.WHITE,
                        x,
                        y,
                        getX() * drawScale.x,
                        getY() * drawScale.y,
                        getAngle(),
                        sclX * effect,
                        sclY);
        }
        //        float width = 16f / 6f;
        //        float height = (320f / 9f) / 20f;
        //        float sclX = width / 600f;
        //        float sclY = height / 400f;
        //        canvas.draw(texture,
        //                    Color.WHITE,
        //                    origin.x,
        //                    origin.y,
        //                    getX(),
        //                    getY(),
        //                    getAngle(),
        //                    sclX * effect,
        //                    sclY);

        //        float width = tilemap.getTileWidth() * textureSclInTiles;
        //        float height = tilemap.getTileHeight() * textureSclInTiles;
        //        float sclX = width / texture.getRegionWidth();
        //        float sclY = height / texture.getRegionHeight();
        //        if (texture != null) {
        //            canvas.draw(texture,
        //                        Color.WHITE,
        //                        origin.x,
        //                        origin.y,
        //                        getX(),
        //                        getY(),
        //                        getAngle(),
        //                        sclX * effect,
        //                        sclY);
        //        }

    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     * <p>
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density", 0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = data.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink", 0) * getWidth() / 2.0f,
                             sensorjv.getFloat("height", 0),
                             sensorCenter,
                             0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }

    /**
     * Returns the name of the ground sensor
     * <p>
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * Applies the force to the body of this dude
     * <p>
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed()) {
            setVX(Math.signum(getVX()) * getMaxSpeed());
        }

        // Don't want to be moving. Damp out player motion
        if (getMovement() == 0f) {
            forceCache.set(-getDamping() * getVX(), 0);
            body.applyForce(forceCache, getPosition(), true);
        } else {
            forceCache.set(getMovement(), 0);
            body.applyForce(forceCache, getPosition(), true);
        }

        // Jump!
        if (isJumping()) {
            if (bouncyTimer > 0) {
                forceCache.set(0, jump_force * bouncyMultiplier);
                SoundController.getInstance().playSound(boingSound);
            } else forceCache.set(0, jump_force);
            body.applyLinearImpulse(forceCache, getPosition(), true);
        }
    }

    /**
     * Returns the upper limit on dude left-right movement.
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Returns left/right movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets left/right movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (movement < 0) {
            faceRight = false;
        } else if (movement > 0) {
            faceRight = true;
        }
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return damping;
    }

    @Override
    public ModelType getType() {
        return ModelType.PLAYER;
    }

    public void setBoingSound(int bs) {
        boingSound = bs;
    }

    //    /**
    //     * Draws the outline of the physics body.
    //     *
    //     * This method can be helpful for understanding issues with collisions.
    //     *
    //     * @param canvas Drawing context
    //     */
    //    public void drawDebug(GameCanvas canvas) {
    //        super.drawDebug(canvas);
    //        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    //    }
}
