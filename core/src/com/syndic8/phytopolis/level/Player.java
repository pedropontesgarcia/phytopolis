package com.syndic8.phytopolis.level;

import com.badlogic.gdx.utils.JsonValue;

public class Player extends GameObject {
    /** The current horizontal movement of the character */
    private float   movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** How long until we can jump again */
    private int jumpCooldown;
    /** Whether we are actively jumping */
    private boolean isJumping;
    /** Whether our feet are on the ground */
    private boolean isGrounded;
    /** Current animation frame for this ship */
    private float animeframe;

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
    public Player(JsonValue data, float width, float height) {
        // The shrink factors fit the image to a tigher hitbox
//        super(	data.get("pos").getFloat(0),
//                data.get("pos").getFloat(1),
//                width*data.get("shrink").getFloat( 0 ),
//                height*data.get("shrink").getFloat( 1 ));
//        setDensity(data.getFloat("density", 0));
//        setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
//        setFixedRotation(true);
//
//        maxspeed = data.getFloat("maxspeed", 0);
//        damping = data.getFloat("damping", 0);
//        force = data.getFloat("force", 0);
//        jump_force = data.getFloat( "jump_force", 0 );
//        jumpLimit = data.getInt( "jump_cool", 0 );
//        shotLimit = data.getInt( "shot_cool", 0 );
//        sensorName = "DudeGroundSensor";
//        this.data = data;

        // Gameplay attributes
        isGrounded = false;
        isJumping = false;
        faceRight = true;

        jumpCooldown = 0;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.PLAYER;
    }
}
