/*
 * Obstacle.java
 *
 * This class and all of the others in this package are provided to give you easy
 * access to Box2d.  Box2d's separation of bodies and shapes (e.g. fixtures) can
 * be a bit daunting. This class combines them together to simplify matters.
 *
 * This, and its superclasses, are fairly robust.  You may want to use
 * them in your game.  They make Box2D a lot easier.
 *
 * Many of the method comments in this class are taken from the Box2d manual by
 * Erin Catto (2011).
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

/**
 * Base model class to support collisions.
 * <p>
 * Instances represents a body and/or a group of bodies.
 * There should be NO game controlling logic code in a physics objects,
 * that should reside in the Controllers.
 * <p>
 * This abstract class has no Body or Shape information and should never
 * be instantiated directly. Instead, you should instantiate either
 * SimplePhysicsObject or ComplexPhysicsObject.  This class only exists
 * to unify common functionality. In particular, it wraps the body and
 * and fixture information into a single interface.
 */
public abstract class GameObject extends Model {

    /// Initialization structures to store body information
    /// Caching objects
    /**
     * Stores the body information for this shape
     */
    protected BodyDef bodyinfo;
    /**
     * Stores the fixture information for this shape
     */
    protected FixtureDef fixture;
    /**
     * The mass data of this shape (which may override the fixture)
     */
    protected MassData massdata;
    /**
     * Whether or not to use the custom mass data
     */
    protected boolean masseffect;
    /**
     * Drawing scale to convert physics units to pixels
     */
    protected Vector2 drawScale;
    /**
     * The physics body for Box2D.
     */
    protected Body body;

    /// Track garbage collection status
    /**
     * A cache value for when the user wants to access the body position
     */
    protected Vector2 positionCache = new Vector2();
    /**
     * A cache value for when the user wants to access the linear velocity
     */
    protected Vector2 velocityCache = new Vector2();
    /**
     * A cache value for when the user wants to access the center of mass
     */
    protected Vector2 centroidCache = new Vector2();
    /**
     * A cache value for when the user wants to access the drawing scale
     */
    protected Vector2 scaleCache = new Vector2();
    /**
     * A tag for debugging purposes
     */
    private String nametag;
    /**
     * Whether the object has changed shape and needs a new fixture
     */
    private boolean isDirty;

    /**
     * Create a new physics object
     *
     * @param x Initial x position in world coordinates
     * @param y Initial y position in world coordinates
     */
    protected GameObject(float x,
                         float y,
                         Tilemap.TilemapParams tmp,
                         float texScl) {
        super(x, y, tmp, texScl);

        // Allocate the body information
        bodyinfo = new BodyDef();
        bodyinfo.awake = true;
        bodyinfo.allowSleep = true;
        bodyinfo.gravityScale = 1.0f;
        bodyinfo.position.set(x, y);
        bodyinfo.fixedRotation = false;
        // Objects are physics objects unless otherwise noted
        bodyinfo.type = BodyType.DynamicBody;

        // Allocate the fixture information
        // Default values are okay
        fixture = new FixtureDef();

        // Allocate the mass information, but turn it off
        masseffect = false;
        massdata = new MassData();

        // Set the default drawing scale
        drawScale = new Vector2(1, 1);

        origin = new Vector2();
        body = null;
    }

    /**
     * Returns the body type for Box2D physics
     * <p>
     * If you want to lock a body in place (e.g. a platform) set this value to STATIC.
     * KINEMATIC allows the object to move (and some limited collisions), but ignores
     * external forces (e.g. gravity). DYNAMIC makes this is a full-blown physics object.
     *
     * @return the body type for Box2D physics
     */
    public BodyType getBodyType() {
        return (body != null ? body.getType() : bodyinfo.type);
    }

    /**
     * Sets the body type for Box2D physics.
     */
    public void setBodyType(BodyType value) {
        if (body != null) {
            body.setType(value);
        } else {
            bodyinfo.type = value;
        }
    }

    /**
     * Returns the current position for this physics body
     * <p>
     * This method does NOT return a reference to the position vector. Changes to this
     * vector will not affect the body.  However, it returns the same vector each time
     * its is called, and so cannot be used as an allocator.
     *
     * @return the current position for this physics body
     */
    public Vector2 getPosition() {
        return (body != null ?
                body.getPosition() :
                positionCache.set(bodyinfo.position));
    }

    /**
     * Sets the current position for this physics body
     * <p>
     * This method does not keep a reference to the parameter.
     *
     * @param value the current position for this physics body
     */
    public void setPosition(Vector2 value) {
        if (body != null) {
            body.setTransform(value, body.getAngle());
        } else {
            bodyinfo.position.set(value);
        }
    }

    /**
     * Returns the x-coordinate for this physics body
     *
     * @return the x-coordinate for this physics body
     */
    public float getX() {
        return (body != null ? body.getPosition().x : bodyinfo.position.x);
    }

    /**
     * Sets the x-coordinate for this physics body
     *
     * @param value the x-coordinate for this physics body
     */
    public void setX(float value) {
        if (body != null) {
            positionCache.set(value, body.getPosition().y);
            body.setTransform(positionCache, body.getAngle());
        } else {
            bodyinfo.position.x = value;
        }
    }

    /**
     * Returns the y-coordinate for this physics body
     *
     * @return the y-coordinate for this physics body
     */
    public float getY() {
        return (body != null ? body.getPosition().y : bodyinfo.position.y);
    }

    /**
     * Sets the y-coordinate for this physics body
     *
     * @param value the y-coordinate for this physics body
     */
    public void setY(float value) {
        if (body != null) {
            positionCache.set(body.getPosition().x, value);
            body.setTransform(positionCache, body.getAngle());
        } else {
            bodyinfo.position.y = value;
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     * <p>
     * This method is called AFTER the collision resolution state. Therefore, it
     * should not be used to process actions or any other gameplay information.  Its
     * primary purpose is to adjust changes to the fixture, which have to take place
     * after collision.
     *
     * @param delta Timing values from parent loop
     */
    public void update(float delta) {
        // Recreate the fixture object if dimensions changed.
        if (isDirty()) {
            createFixtures();
        }
    }

    /**
     * Returns true if the shape information must be updated.
     * <p>
     * Attributes tied to the geometry (and not just forces/position) must wait for
     * collisions to complete before they are reset.  Shapes (and their properties)
     * are reset in the update method.
     *
     * @return true if the shape information must be updated.
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Create new fixtures for this body, defining the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected abstract void createFixtures();

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float width = tilemapParams.tileWidth() * textureSclInTiles;
        float height = tilemapParams.tileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();
        if (texture != null) {
            canvas.draw(texture,
                        Color.WHITE,
                        origin.x,
                        origin.y,
                        getX(),
                        getY(),
                        getAngle(),
                        sclX,
                        sclY);
        }
    }

    /**
     * Returns the angle of rotation for this body (about the center).
     * <p>
     * The value returned is in radians
     *
     * @return the angle of rotation for this body
     */
    public float getAngle() {
        return (body != null ? body.getAngle() : bodyinfo.angle);
    }

    /**
     * Sets the angle of rotation for this body (about the center).
     *
     * @param value the angle of rotation for this body (in radians)
     */
    public void setAngle(float value) {
        if (body != null) {
            body.setTransform(body.getPosition(), value);
        } else {
            bodyinfo.angle = value;
        }
    }

    /**
     * Sets the current position for this physics body
     *
     * @param x the x-coordinate for this physics body
     * @param y the y-coordinate for this physics body
     */
    public void setPosition(float x, float y) {
        if (body != null) {
            positionCache.set(x, y);
            body.setTransform(positionCache, body.getAngle());
        } else {
            bodyinfo.position.set(x, y);
        }
    }

    /**
     * Returns the linear velocity for this physics body
     * <p>
     * This method does NOT return a reference to the velocity vector. Changes to this
     * vector will not affect the body.  However, it returns the same vector each time
     * its is called, and so cannot be used as an allocator.
     *
     * @return the linear velocity for this physics body
     */
    public Vector2 getLinearVelocity() {
        return (body != null ?
                body.getLinearVelocity() :
                velocityCache.set(bodyinfo.linearVelocity));
    }

    /**
     * Sets the linear velocity for this physics body
     * <p>
     * This method does not keep a reference to the parameter.
     *
     * @param value the linear velocity for this physics body
     */
    public void setLinearVelocity(Vector2 value) {
        if (body != null) {
            body.setLinearVelocity(value);
        } else {
            bodyinfo.linearVelocity.set(value);
        }
    }

    /**
     * Returns the x-velocity for this physics body
     *
     * @return the x-velocity for this physics body
     */
    public float getVX() {
        return (body != null ?
                body.getLinearVelocity().x :
                bodyinfo.linearVelocity.x);
    }

    /**
     * Sets the x-velocity for this physics body
     *
     * @param value the x-velocity for this physics body
     */
    public void setVX(float value) {
        if (body != null) {
            velocityCache.set(value, body.getLinearVelocity().y);
            body.setLinearVelocity(velocityCache);
        } else {
            bodyinfo.linearVelocity.x = value;
        }
    }

    /**
     * Returns the y-velocity for this physics body
     *
     * @return the y-velocity for this physics body
     */
    public float getVY() {
        return (body != null ?
                body.getLinearVelocity().y :
                bodyinfo.linearVelocity.y);
    }

    /**
     * Sets the y-velocity for this physics body
     *
     * @param value the y-velocity for this physics body
     */
    public void setVY(float value) {
        if (body != null) {
            velocityCache.set(body.getLinearVelocity().x, value);
            body.setLinearVelocity(velocityCache);
        } else {
            bodyinfo.linearVelocity.y = value;
        }
    }

    /**
     * Returns the angular velocity for this physics body
     * <p>
     * The rate of change is measured in radians per step
     *
     * @return the angular velocity for this physics body
     */
    public float getAngularVelocity() {
        return (body != null ?
                body.getAngularVelocity() :
                bodyinfo.angularVelocity);
    }

    /**
     * Sets the angular velocity for this physics body
     *
     * @param value the angular velocity for this physics body (in radians)
     */
    public void setAngularVelocity(float value) {
        if (body != null) {
            body.setAngularVelocity(value);
        } else {
            bodyinfo.angularVelocity = value;
        }
    }

    /**
     * Returns true if the body is active
     * <p>
     * An inactive body not participate in collision or dynamics. This state is similar
     * to sleeping except the body will not be woken by other bodies and the body's
     * fixtures will not be placed in the broad-phase. This means the body will not
     * participate in collisions, ray casts, etc.
     *
     * @return true if the body is active
     */
    public boolean isActive() {
        return (body != null ? body.isActive() : bodyinfo.active);
    }

    /**
     * Sets whether the body is active
     * <p>
     * An inactive body not participate in collision or dynamics. This state is similar
     * to sleeping except the body will not be woken by other bodies and the body's
     * fixtures will not be placed in the broad-phase. This means the body will not
     * participate in collisions, ray casts, etc.
     *
     * @param value whether the body is active
     */
    public void setActive(boolean value) {
        if (body != null) {
            body.setActive(value);
        } else {
            bodyinfo.active = value;
        }
    }

    /**
     * Returns true if the body is awake
     * <p>
     * An sleeping body is one that has come to rest and the physics engine has decided
     * to stop simulating it to save CPU cycles. If a body is awake and collides with a
     * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a
     * joint or contact attached to them is destroyed.  You can also wake a body manually.
     *
     * @return true if the body is awake
     */
    public boolean isAwake() {
        return (body != null ? body.isAwake() : bodyinfo.awake);
    }

    /**
     * Sets whether the body is awake
     * <p>
     * An sleeping body is one that has come to rest and the physics engine has decided
     * to stop simulating it to save CPU cycles. If a body is awake and collides with a
     * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a
     * joint or contact attached to them is destroyed.  You can also wake a body manually.
     *
     * @param value whether the body is awake
     */
    public void setAwake(boolean value) {
        if (body != null) {
            body.setAwake(value);
        } else {
            bodyinfo.awake = value;
        }
    }

    /**
     * Returns false if this body should never fall asleep
     * <p>
     * An sleeping body is one that has come to rest and the physics engine has decided
     * to stop simulating it to save CPU cycles. If a body is awake and collides with a
     * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a
     * joint or contact attached to them is destroyed.  You can also wake a body manually.
     *
     * @return false if this body should never fall asleep
     */
    public boolean isSleepingAllowed() {
        return (body != null ? body.isSleepingAllowed() : bodyinfo.allowSleep);
    }

    /**
     * Sets whether the body should ever fall asleep
     * <p>
     * An sleeping body is one that has come to rest and the physics engine has decided
     * to stop simulating it to save CPU cycles. If a body is awake and collides with a
     * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a
     * joint or contact attached to them is destroyed.  You can also wake a body manually.
     *
     * @param value whether the body should ever fall asleep
     */
    public void setSleepingAllowed(boolean value) {
        if (body != null) {
            body.setSleepingAllowed(value);
        } else {
            bodyinfo.allowSleep = value;
        }
    }

    /**
     * Returns true if this body is a bullet
     * <p>
     * By default, Box2D uses continuous collision detection (CCD) to prevent dynamic
     * bodies from tunneling through static bodies. Normally CCD is not used between
     * dynamic bodies. This is done to keep performance reasonable. In some game
     * scenarios you need dynamic bodies to use CCD. For example, you may want to shoot
     * a high speed bullet at a stack of dynamic bricks. Without CCD, the bullet might
     * tunnel through the bricks.
     * <p>
     * Fast moving objects in Box2D can be labeled as bullets. Bullets will perform CCD
     * with both static and dynamic bodies. You should decide what bodies should be
     * bullets based on your game design.
     *
     * @return true if this body is a bullet
     */
    public boolean isBullet() {
        return (body != null ? body.isBullet() : bodyinfo.bullet);
    }

    /**
     * Sets whether this body is a bullet
     * <p>
     * By default, Box2D uses continuous collision detection (CCD) to prevent dynamic
     * bodies from tunneling through static bodies. Normally CCD is not used between
     * dynamic bodies. This is done to keep performance reasonable. In some game
     * scenarios you need dynamic bodies to use CCD. For example, you may want to shoot
     * a high speed bullet at a stack of dynamic bricks. Without CCD, the bullet might
     * tunnel through the bricks.
     * <p>
     * Fast moving objects in Box2D can be labeled as bullets. Bullets will perform CCD
     * with both static and dynamic bodies. You should decide what bodies should be
     * bullets based on your game design.
     *
     * @param value whether this body is a bullet
     */
    public void setBullet(boolean value) {
        if (body != null) {
            body.setBullet(value);
        } else {
            bodyinfo.bullet = value;
        }
    }

    /**
     * Returns true if this body be prevented from rotating
     * <p>
     * This is very useful for characters that should remain upright.
     *
     * @return true if this body be prevented from rotating
     */
    public boolean isFixedRotation() {
        return (body != null ? body.isFixedRotation() : bodyinfo.fixedRotation);
    }

    /**
     * Sets whether this body be prevented from rotating
     * <p>
     * This is very useful for characters that should remain upright.
     *
     * @param value whether this body be prevented from rotating
     */
    public void setFixedRotation(boolean value) {
        if (body != null) {
            body.setFixedRotation(value);
        } else {
            bodyinfo.fixedRotation = value;
        }
    }

    /**
     * Returns the gravity scale to apply to this body
     * <p>
     * This allows isolated objects to float.  Be careful with this, since increased
     * gravity can decrease stability.
     *
     * @return the gravity scale to apply to this body
     */
    public float getGravityScale() {
        return (body != null ? body.getGravityScale() : bodyinfo.gravityScale);
    }

    /**
     * Sets the gravity scale to apply to this body
     * <p>
     * This allows isolated objects to float.  Be careful with this, since increased
     * gravity can decrease stability.
     *
     * @param value the gravity scale to apply to this body
     */
    public void setGravityScale(float value) {
        if (body != null) {
            body.setGravityScale(value);
        } else {
            bodyinfo.gravityScale = value;
        }
    }

    /**
     * Returns the linear damping for this body.
     * <p>
     * Linear damping is use to reduce the linear velocity. Damping is different than
     * friction because friction only occurs with contact. Damping is not a replacement
     * for friction and the two effects should be used together.
     * <p>
     * Damping parameters should be between 0 and infinity, with 0 meaning no damping,
     * and infinity meaning full damping. Normally you will use a damping value between
     * 0 and 0.1. Most people avoid linear damping because it makes bodies look floaty.
     *
     * @return the linear damping for this body.
     */
    public float getLinearDamping() {
        return (body != null ?
                body.getLinearDamping() :
                bodyinfo.linearDamping);
    }

    /**
     * Sets the linear damping for this body.
     * <p>
     * Linear damping is use to reduce the linear velocity. Damping is different than
     * friction because friction only occurs with contact. Damping is not a replacement
     * for friction and the two effects should be used together.
     * <p>
     * Damping parameters should be between 0 and infinity, with 0 meaning no damping,
     * and infinity meaning full damping. Normally you will use a damping value between
     * 0 and 0.1. Most people avoid linear damping because it makes bodies look floaty.
     *
     * @param value the linear damping for this body.
     */
    public void setLinearDamping(float value) {
        if (body != null) {
            body.setLinearDamping(value);
        } else {
            bodyinfo.linearDamping = value;
        }
    }

    /**
     * Returns the angular damping for this body.
     * <p>
     * Angular damping is use to reduce the angular velocity. Damping is different than
     * friction because friction only occurs with contact. Damping is not a replacement
     * for friction and the two effects should be used together.
     * <p>
     * Damping parameters should be between 0 and infinity, with 0 meaning no damping,
     * and infinity meaning full damping. Normally you will use a damping value between
     * 0 and 0.1.
     *
     * @return the angular damping for this body.
     */
    public float getAngularDamping() {
        return (body != null ?
                body.getAngularDamping() :
                bodyinfo.angularDamping);
    }

    /**
     * Sets the angular damping for this body.
     * <p>
     * Angular damping is use to reduce the angular velocity. Damping is different than
     * friction because friction only occurs with contact. Damping is not a replacement
     * for friction and the two effects should be used together.
     * <p>
     * Damping parameters should be between 0 and infinity, with 0 meaning no damping,
     * and infinity meaning full damping. Normally you will use a damping value between
     * 0 and 0.1.
     *
     * @param value the angular damping for this body.
     */
    public void setAngularDamping(float value) {
        if (body != null) {
            body.setAngularDamping(value);
        } else {
            bodyinfo.angularDamping = value;
        }
    }

    /**
     * Returns the density of this body
     * <p>
     * The density is typically measured in usually in kg/m^2. The density can be zero or
     * positive. You should generally use similar densities for all your fixtures. This
     * will improve stacking stability.
     *
     * @return the density of this body
     */
    public float getDensity() {
        return fixture.density;
    }

    /**
     * Sets the density of this body
     * <p>
     * The density is typically measured in usually in kg/m^2. The density can be zero or
     * positive. You should generally use similar densities for all your fixtures. This
     * will improve stacking stability.
     *
     * @param value the density of this body
     */
    public void setDensity(float value) {
        fixture.density = value;
        if (body != null) {
            for (Fixture f : body.getFixtureList()) {
                f.setDensity(value);
            }
        }
    }

    /**
     * Returns the friction coefficient of this body
     * <p>
     * The friction parameter is usually set between 0 and 1, but can be any non-negative
     * value. A friction value of 0 turns off friction and a value of 1 makes the friction
     * strong. When the friction force is computed between two shapes, Box2D must combine
     * the friction parameters of the two parent fixtures. This is done with the geometric
     * mean.
     *
     * @return the friction coefficient of this body
     */
    public float getFriction() {
        return fixture.friction;
    }

    /**
     * Sets the friction coefficient of this body
     * <p>
     * The friction parameter is usually set between 0 and 1, but can be any non-negative
     * value. A friction value of 0 turns off friction and a value of 1 makes the friction
     * strong. When the friction force is computed between two shapes, Box2D must combine
     * the friction parameters of the two parent fixtures. This is done with the geometric
     * mean.
     *
     * @param value the friction coefficient of this body
     */
    public void setFriction(float value) {
        fixture.friction = value;
        if (body != null) {
            for (Fixture f : body.getFixtureList()) {
                f.setFriction(value);
            }
        }
    }

    /**
     * Returns the restitution of this body
     * <p>
     * Restitution is used to make objects bounce. The restitution value is usually set
     * to be between 0 and 1. Consider dropping a ball on a table. A value of zero means
     * the ball won't bounce. This is called an inelastic collision. A value of one means
     * the ball's velocity will be exactly reflected. This is called a perfectly elastic
     * collision.
     *
     * @return the restitution of this body
     */
    public float getRestitution() {
        return fixture.restitution;
    }

    /// MassData Methods

    /**
     * Sets the restitution of this body
     * <p>
     * Restitution is used to make objects bounce. The restitution value is usually set
     * to be between 0 and 1. Consider dropping a ball on a table. A value of zero means
     * the ball won't bounce. This is called an inelastic collision. A value of one means
     * the ball's velocity will be exactly reflected. This is called a perfectly elastic
     * collision.
     *
     * @param value the restitution of this body
     */
    public void setRestitution(float value) {
        fixture.restitution = value;
        if (body != null) {
            for (Fixture f : body.getFixtureList()) {
                f.setRestitution(value);
            }
        }
    }

    /**
     * Returns true if this object is a sensor.
     * <p>
     * Sometimes game logic needs to know when two entities overlap yet there should be
     * no collision response. This is done by using sensors. A sensor is an entity that
     * detects collision but does not produce a response.
     *
     * @return true if this object is a sensor.
     */
    public boolean isSensor() {
        return fixture.isSensor;
    }

    /**
     * Sets whether this object is a sensor.
     * <p>
     * Sometimes game logic needs to know when two entities overlap yet there should be
     * no collision response. This is done by using sensors. A sensor is an entity that
     * detects collision but does not produce a response.
     *
     * @param value whether this object is a sensor.
     */
    public void setSensor(boolean value) {
        fixture.isSensor = value;
        if (body != null) {
            for (Fixture f : body.getFixtureList()) {
                f.setSensor(value);
            }
        }
    }

    /**
     * Returns the filter data for this object (or null if there is none)
     * <p>
     * Collision filtering allows you to prevent collision between fixtures. For example,
     * say you make a character that rides a bicycle. You want the bicycle to collide
     * with the terrain and the character to collide with the terrain, but you don't want
     * the character to collide with the bicycle (because they must overlap). Box2D
     * supports such collision filtering using categories and groups.
     *
     * @return the filter data for this object (or null if there is none)
     */
    public Filter getFilterData() {
        return fixture.filter;
    }

    /**
     * Sets the filter data for this object
     * <p>
     * Collision filtering allows you to prevent collision between fixtures. For example,
     * say you make a character that rides a bicycle. You want the bicycle to collide
     * with the terrain and the character to collide with the terrain, but you don't want
     * the character to collide with the bicycle (because they must overlap). Box2D
     * supports such collision filtering using categories and groups.
     * <p>
     * A value of null removes all collision filters.
     *
     * @param value the filter data for this object
     */
    public void setFilterData(Filter value) {
        if (value != null) {
            fixture.filter.categoryBits = value.categoryBits;
            fixture.filter.groupIndex = value.groupIndex;
            fixture.filter.maskBits = value.maskBits;
        } else {
            fixture.filter.categoryBits = 0x0001;
            fixture.filter.groupIndex = 0;
            fixture.filter.maskBits = -1;
        }
        if (body != null) {
            for (Fixture f : body.getFixtureList()) {
                f.setFilterData(value);
            }
        }
    }

    /**
     * Resets this body to use the mass computed from the its shape and density
     */
    public void resetMass() {
        masseffect = false;
        if (body != null) {
            body.resetMassData();
        }
    }    /**
     * Returns the center of mass of this body
     * <p>
     * This method does NOT return a reference to the centroid position. Changes to this
     * vector will not affect the body.  However, it returns the same vector each time
     * its is called, and so cannot be used as an allocator.
     *
     * @return the center of mass for this physics body
     */
    public Vector2 getCentroid() {
        return (body != null ?
                body.getLocalCenter() :
                centroidCache.set(massdata.center));
    }

    /**
     * Sets whether the shape information must be updated.
     * <p>
     * Attributes tied to the geometry (and not just forces/position) must wait for
     * collisions to complete before they are reset.  Shapes (and their properties)
     * are reset in the update method.
     *
     * @param value whether the shape information must be updated.
     */
    public void markDirty(boolean value) {
        isDirty = value;
    }

    /**
     * Returns the Box2D body for this object.
     * <p>
     * You use this body to add joints and apply forces.
     *
     * @return the Box2D body for this object.
     */
    public Body getBody() {
        return body;
    }

    /**
     * Returns the physics object tag.
     * <p>
     * A tag is a string attached to an object, in order to identify it in debugging.
     *
     * @return the physics object tag.
     */
    public String getName() {
        return nametag;
    }

    /**
     * Sets the physics object tag.
     * <p>
     * A tag is a string attached to an object, in order to identify it in debugging.
     *
     * @param value the physics object tag
     */
    public void setName(String value) {
        nametag = value;
    }    /**
     * Sets the center of mass for this physics body
     * <p>
     * This method does not keep a reference to the parameter.
     *
     * @param value the center of mass for this physics body
     */
    public void setCentroid(Vector2 value) {
        if (!masseffect) {
            masseffect = true;
            massdata.I = getInertia();
            massdata.mass = getMass();
        }
        massdata.center.set(value);
        if (body != null) {
            body.setMassData(massdata); // Protected accessor?
        }
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     * <p>
     * Implementations of this method should NOT retain a reference to World.
     * That is a tight coupling that we should avoid.
     *
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // Make a body, if possible
        bodyinfo.active = true;
        body = world.createBody(bodyinfo);
        body.setUserData(this);

        // Only initialize if a body was created.
        if (body != null) {
            createFixtures();
            return true;
        }

        bodyinfo.active = false;
        return false;
    }

    /**
     * Destroys the physics Body(s) of this object if applicable,
     * removing them from the world.
     *
     * @param world Box2D world that stores body
     */
    public void deactivatePhysics(World world) {
        // Should be good for most (simple) applications.
        if (body != null) {
            // Snapshot the values
            setBodyState(body);
            world.destroyBody(body);
            body = null;
            bodyinfo.active = false;
        }
    }

    /**
     * Copies the state from the given body to the body def.
     * <p>
     * This is important if you want to save the state of the body before removing
     * it from the world.
     */
    protected void setBodyState(Body body) {
        bodyinfo.type = body.getType();
        bodyinfo.angle = body.getAngle();
        bodyinfo.active = body.isActive();
        bodyinfo.awake = body.isAwake();
        bodyinfo.bullet = body.isBullet();
        bodyinfo.position.set(body.getPosition());
        bodyinfo.linearVelocity.set(body.getLinearVelocity());
        bodyinfo.allowSleep = body.isSleepingAllowed();
        bodyinfo.fixedRotation = body.isFixedRotation();
        bodyinfo.gravityScale = body.getGravityScale();
        bodyinfo.angularDamping = body.getAngularDamping();
        bodyinfo.linearDamping = body.getLinearDamping();
    }

    /**
     * Release the fixtures for this body, reseting the shape
     * <p>
     * This is the primary method to override for custom physics objects.
     */
    protected abstract void releaseFixtures();    /**
     * Returns the rotational inertia of this body
     * <p>
     * For static bodies, the mass and rotational inertia are set to zero. When
     * a body has fixed rotation, its rotational inertia is zero.
     *
     * @return the rotational inertia of this body
     */
    public float getInertia() {
        return (body != null ? body.getInertia() : massdata.I);
    }







    /**
     * Sets the rotational inertia of this body
     * <p>
     * For static bodies, the mass and rotational inertia are set to zero. When
     * a body has fixed rotation, its rotational inertia is zero.
     *
     * @param value the rotational inertia of this body
     */
    public void setInertia(float value) {
        if (!masseffect) {
            masseffect = true;
            massdata.center.set(getCentroid());
            massdata.mass = getMass();
        }
        massdata.I = value;
        if (body != null) {
            body.setMassData(massdata); // Protected accessor?
        }
    }

    /**
     * Returns the mass of this body
     * <p>
     * The value is usually in kilograms.
     *
     * @return the mass of this body
     */
    public float getMass() {
        return (body != null ? body.getMass() : massdata.mass);
    }

    /// Garbage Collection Methods

    /**
     * Sets the mass of this body
     * <p>
     * The value is usually in kilograms.
     *
     * @param value the mass of this body
     */
    public void setMass(float value) {
        if (!masseffect) {
            masseffect = true;
            massdata.center.set(getCentroid());
            massdata.I = getInertia();
        }
        massdata.mass = value;
        if (body != null) {
            body.setMassData(massdata); // Protected accessor?
        }
    }

    /// DRAWING METHODS

    /// Abstract Methods

}