package com.syndic8.phytopolis.level.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class Leaf extends BoxObject {

    /**
     * Animation speed in frames per second.
     */
    private static final float ANIMATION_SPEED = 10;
    /**
     * Number of frames in the bouncy leaf upgrade filmstrip.
     */
    private static final float NUM_BOUNCY_FRAMES = 7;
    /**
     * Number of frames in the bouncy leaf animation filmstrip.
     */
    private static final float BOUNCE_FRAMES = 6;
    /**
     * Duration of the bouncy leaf animation in seconds.
     */
    private final float BOUNCY_TIMER_MAX = 1;
    private final Color ghostColor = new Color(1, 1, 1, .5f);
    private final InputController ic;
    private leafType type;
    private float health;
    private int healthMark;
    private boolean beingEaten;
    private float bounceFrame;
    private float bounceFrame2;
    private float bouncyTimer = 0;
    private boolean bouncy;
    private FilmStrip bounceTexture;
    private FilmStrip upgradeTexture;
    private boolean sun;

    /**
     * enum containing possible leaf types
     */
    public enum leafType {NORMAL, BOUNCY, NORMAL1, NORMAL2}

    /**
     * Creates a new Leaf object with the specified position and dimensions
     *
     * @param x      x-position
     * @param y      y-position
     * @param width  width of the leaf
     * @param height height of the leaf
     * @param type
     */
    public Leaf(float x,
                float y,
                float width,
                float height,
                leafType type,
                Tilemap.TilemapParams tmp,
                float texScl) {
        super(x, y, width, height, tmp, texScl);
        bodyinfo.type = BodyDef.BodyType.StaticBody;
        this.type = type;
        zIndex = 2;
        health = 5;
        healthMark = 5;
        beingEaten = false;
        bounceFrame = 0;
        bouncy = false;
        sun = false;
        ic = InputController.getInstance();
    }

    @Override
    public ModelType getType() {
        return ModelType.LEAF;
    }

    public void setType(leafType t) {
        type = t;
    }

    public void setTilemapParams(Tilemap.TilemapParams tmp) {
        tilemapParams = tmp;
    }

    public void setTextureScale(float scl) {
        textureSclInTiles = scl;
    }

    public void setBounceTexture(FilmStrip value) {
        bounceTexture = value;
    }

    public void setUpgradeTexture(FilmStrip value) {
        upgradeTexture = value;
    }

    public boolean fullyEaten() {
        return health <= 0;
    }

    public void setBeingEaten(boolean value) {
        beingEaten = value;
    }

    public boolean healthBelowMark() {
        if (health < healthMark) {
            healthMark--;
            return true;
        }
        return false;
    }

    public void setBouncy(boolean bounce) {
        bouncy = bounce;

    }

    public void setSun(boolean value) {
        sun = value;
        animFrame = 2;
        bounceFrame = 2;
    }

    public void setBouncyTimer(int value) {
        bouncyTimer = value;
    }

    /**
     * Updates the state of this object.
     * <p>
     * This method is only intended to update values that change local state in
     * well-defined ways, like position or a cooldown value.  It does not handle
     * collisions (which are determined by the CollisionController).  It is
     * not intended to interact with other objects in any way at all.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        if (getLeafType() != leafType.BOUNCY) {
            if (!sun) {
                if (beingEaten) {
                    health -= dt / 1.2f;
                }
                if (animFrame < 4) {
                    animFrame += ANIMATION_SPEED * dt;
                } else if (health < 5 && health > 0) {
                    animFrame = 4 + (5 - health);
                }
            } else {
                if (animFrame >= 4) {
                    health = 5;
                    sun = false;
                } else if (animFrame < 4) {
                    animFrame += ANIMATION_SPEED * dt;
                }
            }
        } else {
            if (bouncy && ic.didJump()) {
                setFilmStrip(bounceTexture);
                bouncyTimer = BOUNCY_TIMER_MAX;
                bounceFrame2 = 0;
                bouncy = false;
            }
            if (bouncyTimer > 0) {
                if (bounceFrame2 >= BOUNCE_FRAMES) {
                    bounceFrame2 -= BOUNCE_FRAMES;
                }
                if (bounceFrame2 < BOUNCE_FRAMES) {
                    bounceFrame2 += ANIMATION_SPEED * dt * 2;
                }
            } else {
                setFilmStrip(upgradeTexture);
                if (bounceFrame < NUM_BOUNCY_FRAMES) {
                    bounceFrame += ANIMATION_SPEED * dt;
                }
                if (bounceFrame >= NUM_BOUNCY_FRAMES) {
                    bounceFrame -= 1;
                }
            }
            if (!bouncy) {
                bouncyTimer = Math.max(bouncyTimer - dt, 0);
            }

        }

    }

    /**
     * @return the type of this leaf.
     */
    public leafType getLeafType() {
        return type;
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
        if (getLeafType() == leafType.BOUNCY) {
            if (bouncyTimer <= 0) {
                getFilmStrip().setFrame((int) bounceFrame);
            } else {
                getFilmStrip().setFrame((int) bounceFrame2);
            }

        } else {
            getFilmStrip().setFrame((int) animFrame);
        }
        canvas.draw(texture, Color.WHITE, x, y, getX(), getY(), 0, sclX, sclY);
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
        canvas.draw(texture, ghostColor, x, y, getX(), getY(), 0, sclX, sclY);
    }

}
