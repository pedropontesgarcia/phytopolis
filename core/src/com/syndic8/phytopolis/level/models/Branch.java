package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;

public class Branch extends Model {

    private final float angle;
    /**
     * Object position (centered on the texture middle)
     */
    protected Vector2 position;
    /**
     * Whether or not the object should be removed at next timestep.
     */
    protected boolean destroyed;
    /**
     * enum containing possible branch types
     */
    public enum branchType {NORMAL, REINFORCED}
    private branchType type;

    public Branch(float x, float y, float angle, branchType type) {
        position = new Vector2(x, y);
        this.angle = angle;
        this.type = type;
    }

    /**
     * Returns the position of this object (e.g. location of the center pixel)
     * <p>
     * The value returned is a reference to the position vector, which may be
     * modified freely.
     *
     * @return the position of this object
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Returns the x-coordinate of the object position (center).
     *
     * @return the x-coordinate of the object position
     */
    public float getX() {
        return position.x;
    }

    /**
     * Sets the x-coordinate of the object position (center).
     *
     * @param value the x-coordinate of the object position
     */
    public void setX(float value) {
        position.x = value;
    }

    /**
     * Returns the y-coordinate of the object position (center).
     *
     * @return the y-coordinate of the object position
     */
    public float getY() {
        return position.y;
    }

    /**
     * Sets the y-coordinate of the object position (center).
     *
     * @param value the y-coordinate of the object position
     */
    public void setY(float value) {
        position.y = value;
    }

    /**
     * Returns true if this object is destroyed.
     * <p>
     * Objects are not removed immediately when destroyed.  They are garbage collected
     * at the end of the frame.  This tells us whether the object should be garbage
     * collected at the frame end.
     *
     * @return true if this object is destroyed
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Sets whether this object is destroyed.
     * <p>
     * Objects are not removed immediately when destroyed.  They are garbage collected
     * at the end of the frame.  This tells us whether the object should be garbage
     * collected at the frame end.
     *
     * @param value whether this object is destroyed
     */
    public void setDestroyed(boolean value) {
        destroyed = value;
    }

    /**
     * Returns the type of this object.
     *
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    @Override
    public ModelType getType() {
        return ModelType.BRANCH;
    }

    /**
     * returns the branch type of this branch
     * @return the type of branch
     */
    public branchType getBranchType(){ return type;}

    /**
     * changes the branch type to the given value
     * @param t the new branch type to be assigned to this branch
     */
    public void setBranchType(branchType t){type = t;}

    /**
     * Updates the state of this object.
     * <p>
     * This method only is only intended to update values that change local state in
     * well-defined ways, like position or a cooldown value.  It does not handle
     * collisions (which are determined by the CollisionController).  It is
     * not intended to interact with other objects in any way at all.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {

    }

    /**
     * Draws this object to the canvas
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas) {
//        canvas.draw(texture,
//                Color.WHITE,
//                texture.getRegionWidth() / 2.0f,
//                0,
//                position.x,
//                position.y,
//                angle,
//                scale.x/90f,
//                scale.x/90f);
        switch (type){
            case NORMAL:
                canvas.draw(texture,
                        Color.WHITE,
                        texture.getRegionWidth() / 2.0f,
                        0,
                        position.x,
                        position.y,
                        angle,
                        scale.x/90f,
                        scale.x/90f);
                break;
            case REINFORCED:
                canvas.draw(texture,
                        Color.WHITE,
                        texture.getRegionWidth() / 1.87f,
                        texture.getRegionHeight()/4f,
                        position.x,
                        position.y,
                        angle,
                        (scale.x * 0.24f)/90f,
                        (scale.x * 0.24f)/90f);
                break;
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        draw(canvas);
    }

}
