package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.FilmStrip;

public class Player extends CapsuleObject {
    private float animFrame;

    private FilmStrip jumpAnimator;
    private static final int NUM_JUMP_FRAMES = 12 ;
    private static final float ANIMATION_SPEED = 0.4f;
    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;

    /** The factor to multiply by the input */
    private final float force;
    /** The amount to slow the character down */
    private final float damping;
    /** The maximum character speed */
    private final float maxspeed;
    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;
    /** The impulse for the character jump */
    private final float jump_force;
    /** Cooldown (in animation frames) for jumping */
    private final int jumpLimit;
    /** Cooldown (in animation frames) for shooting */
    private final int shotLimit;

    /** The current horizontal movement of the character */
    private float   movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** How long until we can jump again */
    private int jumpCooldown;
    /** Whether we are actively jumping */
    private boolean isJumping;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** Whether our feet are on the ground */
    private boolean isGrounded;
    /** Whether we are actively shooting */
    private boolean isShooting;
    /** The physics shape of this object */
    private PolygonShape sensorShape;

    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();
    /**
     * Multiplier for when standing on bouncy leaf
     */
    private float bouncyMultiplier = 2f;

    /**
     * whether the dude can jump extra high
     */
    private boolean bouncy = false;


    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets left/right movement of this character.
     *
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
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
    public boolean isShooting() {
        return isShooting && shootCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively firing.
     *
     * @param value whether the dude is actively firing.
     */
    public void setShooting(boolean value) {
        isShooting = value;
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
     * sets whether the dude can jump extra high, for example, if the dude is standing on a bouncy leaf
     * @param value whether the dude can jump extra high
     */
    public void setBouncy(boolean value){
        bouncy = value;
    }

    /**
     * Checks whether the player is at the bottom (for water purposes)
     *
     * @return whether or not the player is at the bottom of the game
     */
    public boolean atBottom() {
        return Math.abs(getY() - 2) < .5;
    }

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
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
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    /**
     * Creates a new dude avatar with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for this dude
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Player(JsonValue data, float width,
                  float height, FilmStrip jump) {
        // The shrink factors fit the image to a tigher hitbox
        super(	data.get("pos").getFloat(0),
                data.get("pos").getFloat(1),
                width*data.get("shrink").getFloat( 0 ),
                height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);
        jump_force = data.getFloat( "jump_force", 0);
        jumpLimit = data.getInt( "jump_cool", 0);
        shotLimit = data.getInt( "shot_cool", 0);
        sensorName = "DudeGroundSensor";
        this.data = data;

        // Gameplay attributes
        isGrounded = false;
        isShooting = false;
        isJumping = false;
        faceRight = true;

        animFrame = 0.0f;
        jumpAnimator = jump;
        shootCooldown = 0;
        jumpCooldown = 0;
        setName("dude");
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
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
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = data.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        return true;
    }


    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        // Don't want to be moving. Damp out player motion
        if (getMovement() == 0f) {
            forceCache.set(-getDamping()*getVX(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed()) {
            setVX(Math.signum(getVX())*getMaxSpeed());
        } else {
            forceCache.set(getMovement(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Jump!
        if (isJumping()) {
            if(bouncy) forceCache.set(0, jump_force * bouncyMultiplier);
            else forceCache.set(0, jump_force);
            body.applyLinearImpulse(forceCache,getPosition(),true);
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {

        if(Math.abs(body.getLinearVelocity().y) >= 0.2){
            animFrame += ANIMATION_SPEED;
            if (animFrame >= NUM_JUMP_FRAMES) {
                animFrame -= NUM_JUMP_FRAMES;
            }
        }
        // Apply cooldowns
        if (isJumping()) {
            jumpCooldown = jumpLimit;



        } else {
            jumpCooldown = Math.max(0, jumpCooldown - 1);
        }

        if (isShooting()) {
            shootCooldown = shotLimit;
        } else {
            shootCooldown = Math.max(0, shootCooldown - 1);
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;

        if (Math.abs(body.getLinearVelocity().y) >= 0.2){
            jumpAnimator.setFrame((int)animFrame);
            float x = jumpAnimator.getRegionWidth()/2.0f;
            float y = jumpAnimator.getRegionHeight()/2.0f;
            canvas.draw(
                    jumpAnimator,
                    Color.WHITE,
                    x,
                    y,
                    getX()*drawScale.x,
                    getY()*drawScale.y,
                    getAngle(),
                    0.8f * effect,
                    0.8f
            );
        }else{
            canvas.draw(
                    texture,
                    Color.WHITE,
                    origin.x,
                    origin.y,
                    getX()*drawScale.x,
                    getY()*drawScale.y,
                    getAngle(),
                    1f * effect,
                    1f
            );
        }


    }

    @Override
    public ModelType getType() {
        return ModelType.PLAYER;
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
