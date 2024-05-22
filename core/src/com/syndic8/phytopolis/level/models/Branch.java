package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.Tilemap;

public class Branch extends Model {

    /**
     * Animation speed in frames per second.
     */
    private static final float ANIMATION_SPEED = 10;
    private final Color ghostColor = new Color(1, 1, 1, .5f);
    private float angle;
    private BranchType type;
    private float animFrame;

    /**
     * enum containing possible branch types
     */
    public enum BranchType {NORMAL, REINFORCED}

    public Branch(float x,
                  float y,
                  float angle,
                  BranchType type,
                  Tilemap.TilemapParams tmp,
                  float texScl) {
        super(x, y, tmp, texScl);
        this.angle = angle;
        this.type = type;
        this.animFrame = 0;
        zIndex = 1;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float a) {
        angle = a;
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    @Override
    public ModelType getType() {
        return ModelType.BRANCH;
    }

    /**
     * Updates the state of this object.
     * <p>
     * This method only is only intended to update values that change local state in
     * well-defined ways, like position or a cooldown value.  It does not handle
     * collisions (which are determined by the CollisionController).  It is
     * not intended to interact with other objects in any way at all.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        if (animFrame < getFilmStrip().getSize() - 1) {
            animFrame += ANIMATION_SPEED * dt;
        } else if (animFrame >= getFilmStrip().getSize()) {
            animFrame = getFilmStrip().getSize() - 1;
        }
    }

    /**
     * Draws this object to the canvas
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas) {
        float width = tilemapParams.tileWidth() * textureSclInTiles;
        float height = tilemapParams.tileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();
        float x = texture.getRegionWidth() / 2.0f;
        float y = texture.getRegionHeight() / 2.0f;
        getFilmStrip().setFrame((int) animFrame);
        canvas.draw(texture,
                    Color.WHITE,
                    x,
                    0,
                    getX(),
                    getY(),
                    angle,
                    sclX,
                    sclY);
    }

    /**
     * returns the branch type of this branch
     *
     * @return the type of branch
     */
    public BranchType getBranchType() {
        return type;
    }

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    //    public void drawDebug(GameCanvas canvas) {
    //        draw(canvas);
    //    }

    /**
     * changes the branch type to the given value
     *
     * @param t the new branch type to be assigned to this branch
     */
    public void setBranchType(BranchType t) {
        type = t;
    }

    /**
     * Draws this object to the canvas
     *
     * @param canvas The drawing context
     */
    public void drawGhost(GameCanvas canvas) {
        float width = tilemapParams.tileWidth() * textureSclInTiles;
        float height = tilemapParams.tileHeight() * textureSclInTiles;
        float sclX = width / texture.getRegionWidth();
        float sclY = height / texture.getRegionHeight();
        float x = texture.getRegionWidth() / 2.0f;
        float y = texture.getRegionHeight() / 2.0f;
        texture.setFrame(4);
        canvas.draw(texture,
                    ghostColor,
                    x,
                    0,
                    getX(),
                    getY(),
                    angle,
                    sclX,
                    sclY);
    }

}
