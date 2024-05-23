package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.SoundController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.*;
import com.syndic8.phytopolis.math.IntVector2;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.PooledList;
import com.syndic8.phytopolis.util.RandomController;
import com.syndic8.phytopolis.util.Tilemap;
import edu.cornell.gdiac.audio.SoundEffect;

import java.util.Random;

/**
 * Subcontroller for the plant grid and its nodes.
 */
public class PlantController {

    /**
     * Buffer of nodes above the top of the world.
     */
    private static final int HEIGHT_BUFFER = 2;
    /**
     * Time between propagations of destruction.
     */
    private static final float PLANT_COYOTE_TIME = 0.25f;
    /**
     * The sun controller gets valid x-coordinates from enabled nodes at a
     * certain height. That height is calculated to be the top row minus this
     * buffer.
     */
    private static final int SUN_SPAWN_BUFFER = 4;
    /**
     * The destruction queue. It is populated when a node is destroyed, and
     * then slowly cleared as the now-unsupported plant structure above is
     * progressively destroyed.
     */
    private final Queue<IntVector2> destructionQueue = new Queue<>(3);
    /**
     * A temporary queue used to copy over values from the destruction queue
     * to prevent that changes to the destruction queue affect iteration.
     * Necessary for a proper BFS destruction.
     */
    private final Queue<IntVector2> currentQueue = new Queue<>(3);
    /**
     * A temporary list of next nodes to be checked for potential addition to
     * the destruction queue.
     */
    private final PooledList<IntVector2> nextNodes = new PooledList<>();
    /**
     * Reference to the ResourceController.
     */
    private final ResourceController resourceController;
    /**
     * The singleton instance of the sound controller.
     */
    private final SoundController soundController;
    /**
     * A random number generator to aid with choosing random branch textures.
     */
    private final Random branchChoice;
    /**
     * A temporary set of removed hazards.
     */
    private final ObjectSet<Bug> removedHazards;
    /**
     * Cached vector for miscellaneous use.
     */
    private final Vector2 cacheVector = new Vector2();
    /**
     * Cached integer vector for miscellaneous use.
     */
    private final IntVector2 cacheIntVector = new IntVector2();

    // Begin leaf textures
    protected FilmStrip bounceTexture;
    protected FilmStrip leafTexture;
    protected FilmStrip bouncyLeafTexture;
    protected FilmStrip leafTextureOne;
    protected FilmStrip leafTextureTwo;
    // End leaf textures

    // Begin branch textures
    protected FilmStrip branchTexture;
    protected FilmStrip firstBranchTexture;
    protected FilmStrip secondBranchTexture;
    protected FilmStrip thirdBranchTexture;
    protected FilmStrip staticBranchTexture;
    protected Texture enBranchTextureUp;
    //End branch textures

    /**
     * Indices of the highest plant position.
     */
    private IntVector2 maxPlantIndex;
    /**
     * Ghost leaf for growth previews.
     */
    private Leaf ghostLeaf;
    /**
     * Ghost branch for growth previews.
     */
    private Branch ghostBranch;
    /**
     * Number of frames left until the next propagation of destruction.
     */
    private float plantCoyoteTimeRemaining = 0;
    /**
     * The plant grid, in column-major order. All branches and leaves grow on
     * plant nodes inside the grid.
     */
    private PlantNode[][] plantGrid;
    /**
     * Width of the plant grid.
     */
    private int width;
    /**
     * Height of the plant grid.
     */
    private int height;
    /**
     * Vertical spacing between rows of nodes.
     */
    private float branchLength;
    /**
     * Horizontal spacing between columns of nodes.
     */
    private float horizontalSpacing;
    /**
     * x-coordinate of the origion of this plant.
     */
    private float xOrigin;
    /**
     * y-coordinate of the origin of this plant.
     */
    private float yOrigin;
    /**
     * Record containing level parameters.
     */
    private Tilemap.TilemapParams tilemapParams;

    // Begin sounds
    private int upgradeSound;
    private int destroySound;
    private int leafSound;
    private int errorSound;
    // End sounds

    /**
     * Glow texture for the bottom nodes.
     */
    private Texture glowTexture;

    /**
     * Branch direction from a node.
     */
    public enum BranchDirection {LEFT, MIDDLE, RIGHT}

    /**
     * Initializes a PlantController referencing the given resource controller.
     *
     * @param rc the resource controller reference.
     */
    public PlantController(ResourceController rc) {
        resourceController = rc;
        removedHazards = new ObjectSet<>();
        maxPlantIndex = new IntVector2();
        branchChoice = RandomController.generator;
        soundController = SoundController.getInstance();
    }

    /**
     * @return a random branch filmstrip.
     */
    private FilmStrip getRandomBranchFilmstrip() {
        int num = branchChoice.nextInt(3);
        if (num == 0) {
            return firstBranchTexture;
        } else if (num == 1) {
            return secondBranchTexture;
        } else {
            return thirdBranchTexture;
        }
    }

    /**
     * Resets the PlantController, creating a centered grid of nodes
     * stretching across the world.
     *
     * @param wld the box2d world.
     * @param tmp the tilemap parameters.
     */
    public void reset(World wld, Tilemap.TilemapParams tmp) {
        branchLength = tmp.tileHeight();
        width = Math.round(tmp.tilemapWidth() * (float) Math.sqrt(3));
        height = Math.round(tmp.worldHeight() / branchLength) + HEIGHT_BUFFER;
        float plantWidth =
                branchLength * (float) Math.sqrt(3) * (width - 1) / 2;
        xOrigin = tmp.worldWidth() / 2 - plantWidth / 2;
        yOrigin = 0;
        tilemapParams = tmp;
        removedHazards.clear();
        if (ghostBranch != null) ghostBranch.markRemoved(true);
        if (ghostLeaf != null) ghostLeaf.markRemoved(true);
        ghostBranch = new Branch(0, 0, 0, Branch.BranchType.NORMAL, tmp, 1);
        ghostLeaf = new Leaf(0, 0, 1, 1, Leaf.leafType.NORMAL, tmp, 0.75f);
        plantGrid = new PlantNode[width][height];
        maxPlantIndex.set(-1, -1);
        horizontalSpacing = (float) Math.sqrt(3) * branchLength / 2f;
        initializePlantGrid(wld);
    }

    /**
     * Initializes the <code>plantGrid</code> attribute with a 2D array of
     * nodes, with the correct spacing and offset to create an isometric grid.
     *
     * @param wld the world to query to set enabled/disabled status for nodes.
     */
    private void initializePlantGrid(World wld) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float yOffset = 0;
                if (x % 2 == 1) yOffset = branchLength / 2f;
                float xCoord = (x * horizontalSpacing) + xOrigin;
                float yCoord = yOrigin + yOffset + (y * branchLength);
                float margin = 0.1f;
                final boolean[] hasBody = {false};
                wld.QueryAABB(new QueryCallback() {
                                  @Override
                                  public boolean reportFixture(Fixture fixture) {
                                      if (fixture.getBody().getUserData() instanceof Tile)
                                          hasBody[0] = true;
                                      return false;
                                  }
                              },
                              xCoord - margin,
                              yCoord - margin,
                              xCoord + margin,
                              yCoord + margin);
                if (y == 0 && yOffset == 0) {
                    wld.QueryAABB(new QueryCallback() {
                                      @Override
                                      public boolean reportFixture(Fixture fixture) {
                                          if (fixture.getBody().getUserData() instanceof Tile)
                                              hasBody[0] = true;
                                          return false;
                                      }
                                  },
                                  xCoord - margin,
                                  yCoord + 2 * margin,
                                  xCoord + margin,
                                  yCoord + 4 * margin);
                }
                plantGrid[x][y] = new PlantNode(xCoord,
                                                yCoord,
                                                yOffset != 0,
                                                !hasBody[0],
                                                tilemapParams);
            }
        }
    }

    /**
     * Grows a branch at the desired coordinates. Finds the nearest node to
     * the given coordinates, and determines the best direction given the
     * position of the given coordinates with respect to that node.
     *
     * @param x the x-coordinate to grow the branch at.
     * @param y the y-coordinate to grow the branch at.
     */
    public Branch growBranch(float x, float y) {
        IntVector2 nodeIndex = coordToIndex(x, y);
        int xIndex = nodeIndex.x;
        int yIndex = nodeIndex.y;
        BranchDirection direction = worldToBranch(x, y);
        if (!resourceController.canGrowBranch()) {
            resourceController.setNotEnough(true);
        }
        if (direction == null || !resourceController.canGrowBranch() ||
                !canBuildTowards(xIndex, yIndex, direction) ||
                !plantGrid[xIndex][yIndex].isEnabled()) {
            soundController.playSound(errorSound);
            return null;
        }
        return plantGrid[xIndex][yIndex].makeBranch(direction,
                                                    Branch.BranchType.NORMAL);
    }

    /**
     * Returns whether the player can build towards the given direction from
     * the given node.
     *
     * @param xIndex    x-index of the node.
     * @param yIndex    y-index of the node.
     * @param direction direction to check towards.
     */
    private boolean canBuildTowards(int xIndex,
                                    int yIndex,
                                    BranchDirection direction) {
        int yIndexOffset = plantGrid[xIndex][yIndex].isOffset() ? 1 : 0;
        switch (direction) {
            case LEFT:
                return inBounds(xIndex - 1, yIndex + yIndexOffset) &&
                        isNodeEnabled(xIndex - 1, yIndex + yIndexOffset);
            case RIGHT:
                return inBounds(xIndex + 1, yIndex + yIndexOffset) &&
                        isNodeEnabled(xIndex + 1, yIndex + yIndexOffset);
            case MIDDLE:
                return inBounds(xIndex, yIndex + 1) &&
                        isNodeEnabled(xIndex, yIndex + 1);
        }
        return false;
    }

    /**
     * Returns whether the given node is enabled.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     */
    public boolean isNodeEnabled(int xIndex, int yIndex) {
        if (!inBounds(xIndex, yIndex)) return false;
        return plantGrid[xIndex][yIndex].isEnabled();
    }

    /**
     * Sets the hazard at the given node to the one given.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     * @param h      hazard.
     */
    public void setHazard(int xIndex, int yIndex, Hazard h) {
        plantGrid[xIndex][yIndex].setHazard(h);
    }

    public void removeHazard(int xIndex, int yIndex) {
        plantGrid[xIndex][yIndex].removeHazard();
    }

    /**
     * Returns whether the given node has a hazard.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     */
    public boolean hasHazard(int xIndex, int yIndex) {
        return plantGrid[xIndex][yIndex].hasHazard();
    }

    /**
     * Upgrades the branch at the target node.
     *
     * @param x         x-coordinate of the target node.
     * @param y         y-coord of the target node.
     * @param direction direction of the target branch to upgrade.
     * @param type      type of branch to upgrade to.
     * @return the new branch.
     */
    public Branch upgradeBranch(float x,
                                float y,
                                BranchDirection direction,
                                Branch.BranchType type) {
        IntVector2 nodeIndex = coordToIndex(x, y);
        int xIndex = nodeIndex.x;
        int yIndex = nodeIndex.y;
        if (resourceController.canUpgrade()) {
            resourceController.decrementUpgrade();
            soundController.playSound(upgradeSound);
            plantGrid[xIndex][yIndex].unmakeBranch(direction);
            return plantGrid[xIndex][yIndex].makeBranch(direction, type);
        }
        return null;
    }

    /**
     * Converts the given coordinates to the corresponding node indices in the
     * plant.
     *
     * @param x x-coordinate to be converted.
     * @param y y-coordinate to be converted.
     */
    public IntVector2 coordToIndex(float x, float y) {
        int xIndex = Math.round((x - xOrigin) / horizontalSpacing);
        int yIndex = (int) (
                (y - yOrigin - (branchLength * .5f * (xIndex % 2))) /
                        branchLength);
        return cacheIntVector.set(xIndex, yIndex);
    }

    /**
     * Grows a leaf at the specified node, or tries to upgrade if a leaf
     * already exists.
     *
     * @param x     x-coordinate of the node.
     * @param y     y-coordinate of the node.
     * @param lt    type of leaf to grow.
     * @param width width of the leaf
     * @return the grown leaf object, null if it could not be grown.
     */
    public Leaf growLeaf(float x, float y, Leaf.leafType lt, float width) {
        IntVector2 nodeIndex = coordToIndex(x, y);
        int xIndex = nodeIndex.x;
        int yIndex = nodeIndex.y;
        Leaf l;
        if (plantGrid[xIndex][yIndex].hasLeaf() &&
                plantGrid[xIndex][yIndex].getLeafType() !=
                        Leaf.leafType.BOUNCY) {
            l = growBouncyLeaf(xIndex, yIndex, width);
        } else {
            l = growNormalLeaf(xIndex, yIndex, lt, width);
        }
        if (l != null && l.getY() > getMaxPlantHeight())
            maxPlantIndex.set(xIndex, yIndex);
        return l;
    }

    /**
     * Grows a bouncy leaf at the specified node.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     * @param width  width of the leaf.
     * @return the grown leaf object, null if it could not be grown.
     */
    private Leaf growBouncyLeaf(int xIndex, int yIndex, float width) {
        if (!resourceController.canUpgrade()) {
            resourceController.setNotEnough(true);
            soundController.playSound(errorSound);
            return null;
        }
        plantGrid[xIndex][yIndex].unmakeLeaf();
        resourceController.decrementUpgrade();
        soundController.playSound(upgradeSound);
        return plantGrid[xIndex][yIndex].makeLeaf(Leaf.leafType.BOUNCY, width);
    }

    /**
     * Grows a normal leaf at the specified node.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     * @param lt     type of leaf to grow.
     * @param width  width of the leaf.
     * @return the grown leaf object, null if it could not be grown.
     */
    private Leaf growNormalLeaf(int xIndex,
                                int yIndex,
                                Leaf.leafType lt,
                                float width) {
        if (!resourceController.canGrowLeaf()) {
            resourceController.setNotEnough(true);
        }
        if (!inBounds(xIndex, yIndex) || !resourceController.canGrowLeaf() ||
                plantGrid[xIndex][yIndex].hasLeaf() ||
                (yIndex == 0 && !plantGrid[xIndex][yIndex].isOffset()) ||
                !canGrowAtIndex(xIndex, yIndex)) {
            soundController.playSound(errorSound);
            return null;
        }
        soundController.playSound(leafSound);
        return plantGrid[xIndex][yIndex].makeLeaf(lt, width);

    }

    /**
     * @return the maximum y-coordinate that the plant currently reaches.
     */
    public float getMaxPlantHeight() {
        if (getMaxPlantXIndex() != -1 &&
                inBounds(getMaxPlantXIndex(), getMaxPlantYIndex())) {
            return plantGrid[getMaxPlantXIndex()][getMaxPlantYIndex()].y;
        }
        return 0;
    }

    /**
     * Returns whether the given node is in bounds on the plant grid.
     * <p>
     * <b>This method should be called and its return value checked before
     * performing any operations on plant nodes within any exposed methods. It
     * should not be called on private methods as the condition should be
     * checked before calling them.</b>
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     */
    public boolean inBounds(int xIndex, int yIndex) {
        return xIndex >= 0 && yIndex >= 0 && xIndex < width && yIndex < height;
    }

    /**
     * Returns whether or not a node can be grown at the given index.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-Index of the node.
     */
    public boolean canGrowAtIndex(int xIndex, int yIndex) {
        boolean lowerNode = xIndex % 2 == 0;
        //If this is a node at the base of the plant, return true
        if (yIndex == 0 && lowerNode) return true;
        //If this node is disabled, then we cannot grow at it
        if (!plantGrid[xIndex][yIndex].isEnabled()) return false;
        int yOff = 0;
        if (lowerNode) yOff = 1;

        boolean below = false;
        if (inBounds(xIndex, yIndex - 1))
            below = plantGrid[xIndex][yIndex - 1].hasBranchInDirection(
                    BranchDirection.MIDDLE);
        boolean downLeft = false;
        if (inBounds(xIndex - 1, yIndex - yOff))
            downLeft = plantGrid[xIndex - 1][yIndex -
                    yOff].hasBranchInDirection(BranchDirection.RIGHT);
        boolean downRight = false;
        if (inBounds(xIndex + 1, yIndex - yOff))
            downRight = plantGrid[xIndex + 1][yIndex -
                    yOff].hasBranchInDirection(BranchDirection.LEFT);
        return below || downLeft || downRight;
    }

    /**
     * @return the x-index of the highest plant position.
     */
    public int getMaxPlantXIndex() {
        return maxPlantIndex.x;
    }

    /**
     * @return the y-index of the highest plant position.
     */
    public int getMaxPlantYIndex() {
        return maxPlantIndex.y;
    }

    /**
     * Returns whether there is a leaf at the closest node to the given
     * position.
     *
     * @param pos position vector to check.
     */
    public boolean hasLeaf(Vector2 pos) {
        float x = pos.x;
        float y = pos.y;
        IntVector2 n = coordToIndex(x, y);
        return hasLeaf(n.x, n.y);
    }

    /**
     * Returns whether the given node has a leaf.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     */
    public boolean hasLeaf(int xIndex, int yIndex) {
        return plantGrid[xIndex][yIndex].hasLeaf();
    }

    /**
     * Propagates any destruction queued in the destruction queue using the
     * coyote time constants. This method should be called every frame.
     *
     * @param dt delta time.
     * @return a set of bugs whose leaves were destroyed.
     */
    public ObjectSet<Bug> propagateDestruction(float dt) {
        plantCoyoteTimeRemaining -= dt;
        removedHazards.clear();
        if (destructionQueue.isEmpty()) return removedHazards;
        if (plantCoyoteTimeRemaining > 0) return removedHazards;
        currentQueue.clear();
        destructionQueue.forEach(v -> currentQueue.addLast(v));
        for (IntVector2 n : currentQueue) {
            destroyRecursivelyAt(n.x, n.y);
            destructionQueue.removeValue(n, true);
        }
        return removedHazards;
    }

    /**
     * Recursively destroys the plant upwards from a certain node, populating
     * the destruction queue so that the unsupported section of the plant
     * going upwards will be destroyed progressively using the coyote time
     * constants. Does <b>not</b> recalculate the maximum plant index; this
     * should be done separately after calling this method externally by
     * calling {@link #recalculateMaxPlantIndex()}.
     *
     * @param xIndex the x-index of the node.
     * @param yIndex the y-index of the node.
     */
    private void destroyRecursivelyAt(int xIndex, int yIndex) {
        if (!inBounds(xIndex, yIndex) || nodeIsEmpty(xIndex, yIndex)) return;
        PlantNode n = plantGrid[xIndex][yIndex];
        if (n.getHazard() instanceof Bug bug) removedHazards.add(bug);
        n.unmakeLeaf();
        n.removeHazard();
        for (BranchDirection d : BranchDirection.values()) {
            if (n.hasBranchInDirection(d)) {
                n.unmakeBranch(d);
                IntVector2 next = getNodeTowards(xIndex, yIndex, d);
                if (!canGrowAtIndex(next.x, next.y)) {
                    destructionQueue.addLast(new IntVector2(next.x, next.y));
                    plantCoyoteTimeRemaining = PLANT_COYOTE_TIME;
                }
            }
        }
        soundController.playSound(destroySound);
    }

    /**
     * Returns whether the node at the given indices has no branches nor a leaf.
     *
     * @param xIndex x-index of the node to check.
     * @param yIndex y-index of the node to check.
     */
    public boolean nodeIsEmpty(int xIndex, int yIndex) {
        return plantGrid[xIndex][yIndex].isEmpty();
    }

    /**
     * Returns the indices of the node directly adjacent to the given one in
     * the given direction.
     *
     * @param xIndex    x-index of the node.
     * @param yIndex    y-index of the node.
     * @param direction direction.
     */
    private IntVector2 getNodeTowards(int xIndex,
                                      int yIndex,
                                      BranchDirection direction) {
        int yIndexOffset = plantGrid[xIndex][yIndex].isOffset() ? 1 : 0;
        switch (direction) {
            case LEFT:
                return cacheIntVector.set(xIndex - 1, yIndex + yIndexOffset);
            case RIGHT:
                return cacheIntVector.set(xIndex + 1, yIndex + yIndexOffset);
            case MIDDLE:
                return cacheIntVector.set(xIndex, yIndex + 1);
        }
        return null;
    }

    /**
     * Schedules destruction of the given node by adding it to the
     * destruction queue.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     */
    public void scheduleDestruction(int xIndex, int yIndex) {
        destructionQueue.addFirst(new IntVector2(xIndex, yIndex));
        plantCoyoteTimeRemaining = PLANT_COYOTE_TIME;
    }

    /**
     * Recursively recalculates the maximum plant index values. Should be
     * called once after any destruction operations. <b>Should not under any
     * circumstances be called every frame.</b>
     */
    public void recalculateMaxPlantIndex() {
        cacheIntVector.set(0, 0);
        for (int xIndex = 0; xIndex < width; xIndex++) {
            IntVector2 candidate = calculateHighestFromNode(xIndex, 0);
            if (plantGrid[candidate.x][candidate.y].y >
                    plantGrid[maxPlantIndex.x][maxPlantIndex.y].y)
                cacheIntVector.set(candidate);
        }
        maxPlantIndex = new IntVector2(cacheIntVector);
    }

    /**
     * Recursively calculates the higest plant indices from the given node.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     * @return the highest plant indices from the given node.
     */
    private IntVector2 calculateHighestFromNode(int xIndex, int yIndex) {
        PlantNode n = plantGrid[xIndex][yIndex];
        for (BranchDirection d : BranchDirection.values()) {
            if (n.hasBranchInDirection(d)) {
                IntVector2 next = getNodeTowards(xIndex, yIndex, d);
                IntVector2 highestOnBranch = calculateHighestFromNode(next.x,
                                                                      next.y);
                nextNodes.push(new IntVector2(highestOnBranch));
            }
        }
        IntVector2 highest = new IntVector2(xIndex, yIndex);
        for (IntVector2 next : nextNodes) {
            if (plantGrid[next.x][next.y].y > plantGrid[highest.x][highest.y].y)
                highest = next;
        }
        nextNodes.clear();
        return highest;
    }

    /**
     * @return a list of valid plant x-coordinates towards the top of the
     * level, using the buffer specified as a constant. To be used for sun
     * spawning.
     */
    public PooledList<Float> getPlantXPositions() {
        PooledList<Float> xPositions = new PooledList<>();
        for (int xIndex = 0; xIndex < width; xIndex++) {
            // The -1 is here to access the top row (index is len - 1)
            if (plantGrid[xIndex][height - 1 - SUN_SPAWN_BUFFER].isEnabled())
                xPositions.push(plantGrid[xIndex][0].x);
        }
        return xPositions;
    }

    /**
     * @return the deductions to the timer associated to bugs biting on leaves.
     */
    public int countTimerDeductions() {
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Leaf l = plantGrid[x][y].getLeaf();
                if (l != null && l.healthBelowMark()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Removes the given hazard from any nodes where it is present.
     *
     * @param h hazard to remove.
     */
    public void removeHazardFromNodes(Hazard h) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (plantGrid[x][y].getHazard() == h) {
                    plantGrid[x][y].removeHazard();
                    if (plantGrid[x][y].hasLeaf() &&
                            plantGrid[x][y].getLeaf().fullyEaten()) {
                        plantGrid[x][y].unmakeLeaf();
                    }
                    return;
                }
            }
        }
    }

    /**
     * Removes bugs from dead leaves.
     *
     * @return a set of any removed bugs.
     */
    public ObjectSet<Bug> removeDeadLeafBugs() {
        removedHazards.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (plantGrid[x][y].hasLeaf() &&
                        plantGrid[x][y].getLeaf().fullyEaten()) {
                    Hazard h = plantGrid[x][y].getHazard();
                    if (h instanceof Bug b) {
                        removedHazards.add(b);
                        plantGrid[x][y].removeHazard();
                        plantGrid[x][y].unmakeLeaf();
                    }
                }
            }
        }
        return removedHazards;
    }

    /**
     * Draws a ghost branch preview at the closest growable node in the
     * best direction given a world position. Does not do anything if no good
     * node candidates or directions exist.
     *
     * @param canvas the game canvas.
     * @param x      x-coordinate.
     * @param y      y-coordinate.
     */
    public void drawGhostBranch(GameCanvas canvas, float x, float y) {
        IntVector2 nodeIndex = coordToIndex(x, y);
        int xIndex = nodeIndex.x;
        int yIndex = nodeIndex.y;
        BranchDirection direction = worldToBranch(x, y);
        // Checks!
        if (!inBounds(xIndex, yIndex)) return;
        if (direction == null) return;
        if (plantGrid[xIndex][yIndex].hasBranchInDirection(direction)) return;
        if (!canBuildTowards(xIndex, yIndex, direction)) return;
        if (!canGrowAtIndex(xIndex, yIndex)) return;
        if (!resourceController.canGrowBranch()) return;
        // End checks
        float angle;
        switch (direction) {
            case MIDDLE:
                angle = 0;
                break;
            case LEFT:
                angle = (float) Math.PI / 3;
                break;
            case RIGHT:
                angle = (float) -Math.PI / 3;
                break;
            default:
                angle = 0;
        }
        ghostBranch.setX(plantGrid[xIndex][yIndex].getX());
        ghostBranch.setY(plantGrid[xIndex][yIndex].getY());
        ghostBranch.setAngle(angle);
        ghostBranch.setFilmStrip(branchTexture);
        ghostBranch.drawGhost(canvas);

    }

    /**
     * Converts a position to a branch direction.
     *
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     * @return the best branch direction, or null if the position is out of
     * bounds.
     */
    public BranchDirection worldToBranch(float x, float y) {
        // Convert screen coordinates to grid indices
        IntVector2 nodeIndex = coordToIndex(x, y);
        int xIndex = nodeIndex.x;
        int yIndex = nodeIndex.y;
        if (!isNodeEnabled(xIndex, yIndex)) return null;

        // Convert indices back to world coordinates to find the center of the cell
        Vector2 cellCenter = indexToWorldCoord(xIndex, yIndex);

        // Calculate angle from the center of the node to the click position
        float angle = (float) Math.atan2(y - cellCenter.y, x - cellCenter.x);

        // Normalize angle into a range from 0 to 2*PI
        angle = (angle + (float) (2 * Math.PI)) % (float) (2 * Math.PI);
        if (angle >= Math.PI) return null;

        // Divide the space around the node into six segments (each segment is 60 degrees)
        BranchDirection direction = getBranchDirection(angle);

        // Grow branch
        if (!branchExists(xIndex, yIndex, direction) &&
                canGrowAtIndex(xIndex, yIndex)) return direction;
        return null;
    }

    /**
     * Returns the world coordinates of the node at the specified index.
     *
     * @param xIndex x-index of the node.
     * @param yIndex y-index of the node.
     */
    public Vector2 indexToWorldCoord(int xIndex, int yIndex) {
        PlantNode n = plantGrid[xIndex][yIndex];
        return cacheVector.set(n.x, n.y);
    }

    /**
     * Returns the best branch direction given an angle.
     *
     * @param angle the angle to check.
     */
    private BranchDirection getBranchDirection(float angle) {
        BranchDirection direction;
        if (angle < Math.PI / 3) {
            direction = BranchDirection.RIGHT;
        } else if (angle < 2 * Math.PI / 3) {
            direction = BranchDirection.MIDDLE;
        } else if (angle < Math.PI) {
            direction = BranchDirection.LEFT;
        }
        // For branches under the node
        else if (angle < 4 * Math.PI / 3) {
            direction = BranchDirection.RIGHT;
        } else if (angle < 5 * Math.PI / 3) {
            direction = BranchDirection.MIDDLE;
        } else {
            direction = BranchDirection.LEFT;
        }
        return direction;
    }

    /**
     * Returns whether or not a branch exists at the given x and y, in the
     * given direction.
     *
     * @param xIndex    x-index of the queried node.
     * @param yIndex    y-inxed of the queried node.
     * @param direction direction of the queried node.
     */
    public boolean branchExists(int xIndex,
                                int yIndex,
                                BranchDirection direction) {
        return (inBounds(xIndex, yIndex) &&
                plantGrid[xIndex][yIndex].hasBranchInDirection(direction));
    }

    /**
     * Draws a ghost leaf preview at the closest growable node given a world
     * position. Does not do anything if no good node candidates exist.
     *
     * @param canvas the game canvas.
     * @param x      x-coordinate.
     * @param y      y-coordinate.
     */
    public void drawGhostLeaf(GameCanvas canvas,
                              Leaf.leafType type,
                              float leafWidth,
                              float x,
                              float y) {
        IntVector2 nodeIndex = coordToIndex(x, y);
        int xIndex = nodeIndex.x;
        int yIndex = nodeIndex.y;
        // Checks!
        if (!inBounds(xIndex, yIndex)) return;
        if (plantGrid[xIndex][yIndex].hasLeaf()) return;
        if (yIndex <= 0 && !plantGrid[xIndex][yIndex].isOffset()) return;
        if (!canGrowAtIndex(xIndex, yIndex)) return;
        if (!resourceController.canGrowLeaf()) return;
        // End checks
        float xl = plantGrid[xIndex][yIndex].getX();
        float yl = plantGrid[xIndex][yIndex].getY();
        ghostLeaf.setPosition(xl, yl);
        ghostLeaf.setDimension(leafWidth, PlantNode.LEAF_HEIGHT);
        ghostLeaf.setType(type);
        switch (type) {
            case NORMAL:
                ghostLeaf.setFilmStrip(leafTexture);
                break;
            case BOUNCY:
                ghostLeaf.setFilmStrip(bouncyLeafTexture);
                break;
            case NORMAL1:
                ghostLeaf.setFilmStrip(leafTextureOne);
                break;
            case NORMAL2:
                ghostLeaf.setFilmStrip(leafTextureTwo);
                break;
        }
        ghostLeaf.drawGhost(canvas);
    }

    /**
     * Returns whether or not a node can be grown at the given position.
     *
     * @param x x-coordinate of the node.
     * @param y y-coordinate of the node.
     */
    public boolean canGrowAt(float x, float y) {
        IntVector2 nodeIndex = coordToIndex(x, y);
        int xIndex = nodeIndex.x;
        int yIndex = nodeIndex.y;
        return canGrowAtIndex(xIndex, yIndex);
    }

    /**
     * Draws glow dots at the enabled bottom nodes.
     *
     * @param canvas the canvas.
     */
    public void drawGlow(GameCanvas canvas) {
        float sclX = tilemapParams.tileWidth() / glowTexture.getWidth();
        float sclY = tilemapParams.tileHeight() / glowTexture.getHeight();
        for (PlantNode[] col : plantGrid) {
            if (!col[0].isOffset() && col[0].isEnabled()) {
                canvas.draw(glowTexture,
                            Color.WHITE,
                            glowTexture.getWidth() / 2f,
                            0,
                            col[0].x,
                            col[0].y,
                            0,
                            sclX,
                            sclY);
            }
        }
    }

    /**
     * @return the width of the plant grid.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the plant grid.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the reference to the resource controller.
     */
    public ResourceController getResourceController() {
        return resourceController;
    }

    /**
     * Gathers all the plant-related assets.
     */
    //TODO are any unused?
    public void gatherAssets(AssetDirectory directory) {
        branchTexture = new FilmStrip(directory.getEntry("gameplay:branch",
                                                         Texture.class),
                                      1,
                                      5,
                                      5);
        firstBranchTexture = new FilmStrip(directory.getEntry("gameplay:branch1",
                                                              Texture.class),
                                           1,
                                           5,
                                           5);
        secondBranchTexture = new FilmStrip(directory.getEntry(
                "gameplay:branch2",
                Texture.class), 1, 5, 5);
        thirdBranchTexture = new FilmStrip(directory.getEntry("gameplay:branch3",
                                                              Texture.class),
                                           1,
                                           5,
                                           5);
        staticBranchTexture = new FilmStrip(directory.getEntry("gameplay:branch",
                                                               Texture.class),
                                            1,
                                            5,
                                            5);

        glowTexture = directory.getEntry("gameplay:glow", Texture.class);
        staticBranchTexture.setFrame(4);
        leafTexture = new FilmStrip(directory.getEntry("gameplay:leaf",
                                                       Texture.class), 1, 9, 9);
        leafTextureOne = new FilmStrip(directory.getEntry("gameplay:leaf1",
                                                          Texture.class),
                                       1,
                                       9,
                                       9);
        leafTextureTwo = new FilmStrip(directory.getEntry("gameplay:leaf2",
                                                          Texture.class),
                                       1,
                                       9,
                                       9);
        bouncyLeafTexture = new FilmStrip(directory.getEntry("gameplay:bouncy",
                                                             Texture.class),
                                          1,
                                          7);
        bounceTexture = new FilmStrip(directory.getEntry(
                "gameplay:bouncy_bounce",
                Texture.class), 1, 6);
        enBranchTextureUp = directory.getEntry("gameplay:enbranch",
                                               Texture.class);
        SoundEffect upgrade = directory.getEntry("upgradeleaf",
                                                 SoundEffect.class);
        upgradeSound = soundController.addSoundEffect(upgrade);
        destroySound = soundController.addSoundEffect(directory.getEntry(
                "destroyplant2",
                SoundEffect.class));
        SoundEffect leafSoundEffect = directory.getEntry("growleaf",
                                                         SoundEffect.class);
        leafSound = soundController.addSoundEffect(leafSoundEffect);
        errorSound = soundController.addSoundEffect(directory.getEntry(
                "errorsound",
                SoundEffect.class));
    }

    /**
     * Returns the type of branch at the given indices and direction, null if
     * no branch.
     *
     * @param xIndex x-index of the node to check.
     * @param yIndex y-index of the node to check.
     */
    public Branch.BranchType getBranchType(int xIndex,
                                           int yIndex,
                                           BranchDirection direction) {
        return plantGrid[xIndex][yIndex].getBranchType(direction);
    }

    /**
     * Returns the type of leaf at the given indices, null if no leaf.
     *
     * @param xIndex x-index of the node to check.
     * @param yIndex y-index of the node to check.
     */
    public Leaf.leafType getLeafType(int xIndex, int yIndex) {
        return plantGrid[xIndex][yIndex].getLeafType();
    }

    /**
     * Returns whether the given column of the plant grid is offset.
     *
     * @param col the column to check.
     */
    public boolean isColumnOffset(int col) {
        return plantGrid[col][0].isOffset();
    }

    /**
     * Node in the plant grid.
     */
    public class PlantNode {

        /**
         * Leaf height.
         */
        public static final float LEAF_HEIGHT = 0.1f;
        /**
         * Leaf width.
         */
        public final float leafWidth = 1.5f;
        /**
         * x-coordinate of this node.
         */
        private final float x;
        /**
         * y-coordinate of this node.
         */
        private final float y;
        /**
         * Tilemap parameters.
         */
        private final Tilemap.TilemapParams tilemapParams;
        /**
         * Whether this node is vertically offset.
         */
        private final boolean isOffset;
        /**
         * Whether this node is enabled.
         */
        private final boolean enabled;
        /**
         * The branch facing left at this node, null if none.
         */
        private Branch left;
        /**
         * The branch facing upward at this node, null if none.
         */
        private Branch middle;
        /**
         * The branch facing right at this node, null if none.
         */
        private Branch right;
        /**
         * The leaf at this node, null if none.
         */
        private Leaf leaf;
        /**
         * The hazard at this node, null if none.
         */
        private Hazard hazard;

        /**
         * Initializes a new PlantNode object with its coordinates, whether
         * it is vertically offset, whether it is enabled, and a record of
         * tilemap parameters.
         *
         * @param x     x-coordinate of the node.
         * @param y     y-coordinate of the node.
         * @param isOff whether this node is vertically offset.
         * @param on    whether this node is enabled.
         * @param tmp   record of tilemap parameters.
         */
        public PlantNode(float x,
                         float y,
                         boolean isOff,
                         boolean on,
                         Tilemap.TilemapParams tmp) {
            this.x = x;
            this.y = y;
            isOffset = isOff;
            enabled = on;
            tilemapParams = tmp;
        }

        /**
         * Makes and returns a branch in the given direction, of the given
         * type, at this node.
         *
         * @param direction branch direction.
         * @param type      branch type.
         */
        public Branch makeBranch(BranchDirection direction,
                                 Branch.BranchType type) {
            float pi = (float) Math.PI;
            Branch newBranch = null;
            IntVector2 nodeIndex = coordToIndex(x, y);
            int xIndex = nodeIndex.x;
            int yIndex = nodeIndex.y;
            int yIndexOffset = isOffset() ? 1 : 0;
            float maxY;
            switch (direction) {
                case LEFT:
                    left = new Branch(x, y, pi / 3, type, tilemapParams, 1);
                    newBranch = left;
                    maxY = plantGrid[xIndex - 1][yIndex + yIndexOffset].y;
                    if (maxY > getMaxPlantHeight())
                        maxPlantIndex.set(xIndex - 1, yIndex + yIndexOffset);
                    break;
                case MIDDLE:
                    middle = new Branch(x, y, 0, type, tilemapParams, 1);
                    newBranch = middle;
                    maxY = plantGrid[xIndex][yIndex + 1].y;
                    if (maxY > getMaxPlantHeight())
                        maxPlantIndex.set(xIndex, yIndex + 1);
                    break;
                case RIGHT:
                    right = new Branch(x, y, -pi / 3, type, tilemapParams, 1);
                    newBranch = right;
                    maxY = plantGrid[xIndex + 1][yIndex + yIndexOffset].y;
                    if (maxY > getMaxPlantHeight())
                        maxPlantIndex.set(xIndex + 1, yIndex + yIndexOffset);
                    break;
            }
            switch (type) {
                case NORMAL:
                    newBranch.setFilmStrip(getRandomBranchFilmstrip());
                    break;
                case REINFORCED:
                    newBranch.setTexture(enBranchTextureUp);
                    break;
            }
            resourceController.decrementGrowBranch();
            return newBranch;

        }

        /**
         * @return whether this node is vertically offset.
         */
        public boolean isOffset() {
            return isOffset;
        }

        /**
         * @return whether this node is enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Makes and returns a leaf of the given type at this node.
         *
         * @param type type of leaf.
         */
        public Leaf makeLeaf(Leaf.leafType type, float width) {
            if (type == Leaf.leafType.BOUNCY) width = leafWidth;
            leaf = new Leaf(x,
                            y,
                            width,
                            LEAF_HEIGHT,
                            type,
                            tilemapParams,
                            0.75f);
            switch (type) {
                case NORMAL:
                    leaf.setFilmStrip(leafTexture);
                    break;
                case BOUNCY:
                    leaf.setBounceTexture(bounceTexture);
                    leaf.setUpgradeTexture(bouncyLeafTexture);
                    break;
                case NORMAL1:
                    leaf.setFilmStrip(leafTextureOne);
                    break;
                case NORMAL2:
                    leaf.setFilmStrip(leafTextureTwo);
                    break;
            }
            resourceController.decrementGrowLeaf();
            return leaf;
        }

        /**
         * @return whether this node has a branch.
         */
        public boolean hasBranch() {
            return hasBranchInDirection(BranchDirection.LEFT) ||
                    hasBranchInDirection(BranchDirection.RIGHT) ||
                    hasBranchInDirection(BranchDirection.MIDDLE);
        }

        /**
         * Returns whether this node has a branch in the given direction.
         *
         * @param direction direction to check for a branch towards.
         */
        public boolean hasBranchInDirection(BranchDirection direction) {
            switch (direction) {
                case MIDDLE:
                    return middle != null && !middle.isRemoved();
                case LEFT:
                    return left != null && !left.isRemoved();
                case RIGHT:
                    return right != null && !right.isRemoved();
                default:
                    return false;
            }
        }

        /**
         * Returns the branch at the given direction.
         *
         * @param direction direction to check for.
         */
        public Branch getBranch(BranchDirection direction) {
            switch (direction) {
                case MIDDLE:
                    return middle;
                case LEFT:
                    return left;
                case RIGHT:
                    return right;
                default:
                    return null;
            }
        }

        /**
         * @return the leaf of this node, null if no leaf.
         */
        public Leaf getLeaf() {
            return leaf;
        }

        /**
         * @return the x-coordinate of this node's position.
         */
        public float getX() {
            return x;
        }

        /**
         * @return the y-coordinate of this node's position.
         */
        public float getY() {
            return y;
        }

        /**
         * Resets this node, removing any branches, leaves or hazards.
         */
        public void reset() {
            if (middle != null) {
                unmakeBranch(BranchDirection.MIDDLE);
            }
            if (left != null) {
                unmakeBranch(BranchDirection.LEFT);
            }
            if (right != null) {
                unmakeBranch(BranchDirection.RIGHT);
            }
            unmakeLeaf();
            removeHazard();
        }

        /**
         * Removes any branches from this node.
         */
        public void unmakeBranch(BranchDirection direction) {
            switch (direction) {
                case MIDDLE:
                    if (middle != null) {
                        middle.markRemoved(true);
                        middle = null;
                    }
                    break;
                case RIGHT:
                    if (right != null) {
                        right.markRemoved(true);
                        right = null;
                    }
                    break;
                case LEFT:
                    if (left != null) {
                        left.markRemoved(true);
                        left = null;
                    }
                    break;
            }
        }

        /**
         * Removes any leaf from this node.
         */
        public void unmakeLeaf() {
            if (leaf != null) {
                leaf.markRemoved(true);
                leaf = null;
            }
        }

        /**
         * Removes any hazards from this node. Does not remove them from the
         * world.
         */
        public void removeHazard() {
            hazard = null;
            if (leaf != null) {
                leaf.setBeingEaten(false);
            }
        }

        /**
         * @return this node's hazard, null if none.
         */
        public Hazard getHazard() {
            return hazard;
        }

        /**
         * Sets this node's hazard to the one given.
         *
         * @param h hazard.
         */
        public void setHazard(Hazard h) {
            hazard = h;
            if (leaf != null && h.getType() == Model.ModelType.BUG &&
                    leaf.getLeafType() != Leaf.leafType.BOUNCY) {
                leaf.setBeingEaten(true);
            }
        }

        /**
         * @return whether this node has a hazard.
         */
        public boolean hasHazard() {
            return hazard != null;
        }

        /**
         * @return whether this node is empty.
         */
        public boolean isEmpty() {
            return !(hasBranchInDirection(BranchDirection.LEFT) ||
                    hasBranchInDirection(BranchDirection.RIGHT) ||
                    hasBranchInDirection(BranchDirection.MIDDLE) || hasLeaf());
        }

        /**
         * @return whether this node has a leaf.
         */
        public boolean hasLeaf() {
            return leaf != null;
        }

        /**
         * Returns the type of branch in the given direction, null if no branch.
         *
         * @param direction the direction of the branch.
         */
        public Branch.BranchType getBranchType(BranchDirection direction) {
            switch (direction) {
                case LEFT:
                    if (hasBranchInDirection(BranchDirection.LEFT))
                        return left.getBranchType();
                    else return null;
                case RIGHT:
                    if (hasBranchInDirection(BranchDirection.RIGHT))
                        return right.getBranchType();
                    else return null;
                case MIDDLE:
                    if (hasBranchInDirection(BranchDirection.MIDDLE))
                        return middle.getBranchType();
                    else return null;
            }
            //There is no branch at this node
            return null;
        }

        /**
         * Sets the type of the branch in the given direction and resets its
         * texture.
         *
         * @param direction the direction of the branch.
         * @param btype     the branch type.
         */
        public void setBranchType(BranchDirection direction,
                                  Branch.BranchType btype) {
            switch (direction) {
                case LEFT:
                    if (hasBranchInDirection(BranchDirection.LEFT)) {
                        left.setBranchType(btype);
                        left.setFilmStrip(getRandomBranchFilmstrip());
                    }
                    break;
                case RIGHT:
                    if (hasBranchInDirection(BranchDirection.RIGHT)) {
                        right.setBranchType(btype);
                        right.setFilmStrip(getRandomBranchFilmstrip());
                    }
                    break;
                case MIDDLE:
                    if (hasBranchInDirection(BranchDirection.MIDDLE)) {
                        middle.setBranchType(btype);
                        middle.setFilmStrip(getRandomBranchFilmstrip());
                    }
                    break;
            }
        }

        /**
         * @return the type of leaf at this node, null if no leaf.
         */
        public Leaf.leafType getLeafType() {
            if (hasLeaf()) return leaf.getLeafType();
            else return null;
        }

    }

}
