package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Queue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Branch;
import com.syndic8.phytopolis.level.models.Leaf;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class PlantController {

    /**
     * the grid containing all possible nodes for a branch to grow from
     */
    private final PlantNode[][] plantGrid;
    /**
     * width of the plant grid
     */
    private final int width;
    /**
     * height of the plant grid
     */
    private final int height;
    /**
     * how far apart to space each node on screen
     */
    private final float gridSpacing;
    /**
     * how far apart each node is on the x axis
     */
    private final float xSpacing;
    /**
     * x coordinate of the origin of this plant
     */
    private final float xOrigin;
    /**
     * y coordinate of the origin of this plant
     */
    private final float yOrigin;
    /**
     * world used for storing physics objects
     */
    private final World world;
    private final Vector2 scale;
    /**
     * conversion ratio from world units to pixels
     */
    private final float worldToPixelConversionRatio;
    /**
     * how many frames between propagations of destruction
     */
    private final int plantCoyoteTime = 15;
    private final Queue<int[]> destructionQueue = new Queue<>();
    /**
     * Reference to the ResourceController
     */
    private final ResourceController resourceController;
    private final Tilemap tilemap;
    private final Vector2 maxLeafIndex;
    /**
     * node texture
     */
    protected Texture nodeTexture;
    /**
     * branch texture
     */
    protected FilmStrip branchTexture;
    /**
     * static branch texture
     */
    protected FilmStrip staticBranchTexture;
    /**
     * leaf texture
     */
    protected FilmStrip leafTexture;
    /**
     * reinforced upwards branch texture
     */
    protected Texture enBranchTextureUp;
    /**
     * bouncy leaf texture
     */
    protected Texture bouncyLeafTexture;
    /**
     * how many more frames until the next propagation of destruction
     */
    private int plantCoyoteTimeRemaining = 0;
    private FilmStrip leafTextureOne;
    private FilmStrip leafTextureTwo;

    /**
     * Initialize a PlantController with specified height and width
     *
     * @param height      the height of the plant grid
     * @param width       the width of the plant grid
     * @param gridSpacing the spacing between nodes of the plant in world units
     * @param world       world to assign physics objects to
     * @param xOrigin     x origin of the plant in world units
     * @param yOrigin     y origin of the plant in world units
     */
    public PlantController(int width,
                           int height,
                           float gridSpacing,
                           float xOrigin,
                           float yOrigin,
                           World world,
                           Vector2 scale,
                           ResourceController rc,
                           Tilemap tm) {
        plantGrid = new PlantNode[width][height];
        this.world = world;
        worldToPixelConversionRatio = 1;
        this.scale = scale;
        this.xOrigin = xOrigin * worldToPixelConversionRatio;
        this.yOrigin = yOrigin * worldToPixelConversionRatio;
        this.width = width;
        this.height = height;
        maxLeafIndex = new Vector2(-1, -1);
        this.gridSpacing = gridSpacing * worldToPixelConversionRatio;
        this.xSpacing = (float) (Math.sqrt(
                (this.gridSpacing * this.gridSpacing) -
                        ((this.gridSpacing / 2f) * (this.gridSpacing / 2f))));
        this.resourceController = rc;
        tilemap = tm;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float yOffset = 0;
                //set the yOffset
                if (x % 2 == 1) yOffset = this.gridSpacing / 2f;
                plantGrid[x][y] = new PlantNode(
                        (x * this.xSpacing) + this.xOrigin,
                        this.yOrigin + yOffset + (y * this.gridSpacing),
                        this.worldToPixelConversionRatio,
                        tilemap);
            }
        }
    }

    public Leaf.leafType getLevelLeaf(String l){
        switch(l){
            case "gameplay:lvl1":
                return Leaf.leafType.NORMAL;
            case "gameplay:lvl2":
                return Leaf.leafType.NORMAL1;
            case "gameplay:lvl3":
                return Leaf.leafType.NORMAL2;
        }
        return null;
    }

    public void reset() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                plantGrid[x][y].reset();
            }
        }
    }

    /**
     * grows a branch at the desired position
     *
     * @param x the x coordinate of the node to grow the branch at
     * @param y the y coordinate of the node to grow the branch at
     */
    public Branch growBranch(float x, float y) {
        int xIndex = worldCoordToIndex(x, y)[0];
        int yIndex = worldCoordToIndex(x, y)[1];
        branchDirection direction = worldToBranch(x, y);
        if (direction == null || !resourceController.canGrowBranch())
            return null;
        return plantGrid[xIndex][yIndex].makeBranch(direction,
                                                    Branch.branchType.NORMAL,
                                                    world);
    }

    /**
     * Converts a world coordinate to the corresponding node index in the plant
     *
     * @param xArg x coordinate to be converted
     * @param yArg y coordinate to be converted
     * @return (x, y) index of the corresponding node
     */
    public int[] worldCoordToIndex(float xArg, float yArg) {
        return screenCoordToIndex(xArg * worldToPixelConversionRatio,
                                  yArg * worldToPixelConversionRatio);
    }

    /**
     * returns a branch at the given screen coords
     *
     * @param x the x coordinate of the node to grow the branch at
     * @param y the y coordinate of the node to grow the branch at
     */
    public branchDirection worldToBranch(float x, float y) {
        // Convert screen coordinates to grid indices
        int xIndex = worldCoordToIndex(x, y)[0];
        int yIndex = worldCoordToIndex(x, y)[1];
        if (!inBounds(xIndex, yIndex)) return null;

        // Convert indices back to world coordinates to find the center of the cell
        Vector2 cellCenter = indexToWorldCoord(xIndex, yIndex);

        // Calculate angle from the center of the node to the click position
        float angle = (float) Math.atan2(y - cellCenter.y, x - cellCenter.x);

        // Normalize angle into a range from 0 to 2*PI
        angle = (angle + (float) (2 * Math.PI)) % (float) (2 * Math.PI);
        if (angle >= Math.PI) return null;

        // Divide the space around the node into six segments (each segment is 60 degrees)
        branchDirection direction = getBranchDirection(angle);

        // Grow branch
        if (!branchExists(xIndex, yIndex, direction) &&
                canGrowAtIndex(xIndex, yIndex)) return direction;
        return null;
    }

    /**
     * Converts a screen coordinate to the corresponding node index in the plant
     *
     * @param xArg x coordinate to be converted
     * @param yArg y coordinate to be converted
     * @return (x, y) index of the corresponding node
     */
    public int[] screenCoordToIndex(float xArg, float yArg) {
        int xIndex = Math.round((xArg - xOrigin) / xSpacing);
        int yIndex = (int) (
                (yArg - yOrigin - (gridSpacing * .5f * (xIndex % 2))) /
                        gridSpacing);
        return new int[]{xIndex, yIndex};
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * returns the world coordinates of the node at the specified index
     *
     * @param xArg x index
     * @param yArg y index
     * @return vector of world coordinates
     */
    public Vector2 indexToWorldCoord(int xArg, int yArg) {
        PlantNode n = plantGrid[xArg][yArg];
        return new Vector2(n.x / worldToPixelConversionRatio,
                           n.y / worldToPixelConversionRatio);
    }

    private branchDirection getBranchDirection(float angle) {
        branchDirection direction;
        if (angle < Math.PI / 3) {
            direction = branchDirection.RIGHT;
        } else if (angle < 2 * Math.PI / 3) {
            direction = branchDirection.MIDDLE;
        } else if (angle < Math.PI) {
            direction = branchDirection.LEFT;
        }
        // For branches under the node
        else if (angle < 4 * Math.PI / 3) {
            direction = branchDirection.RIGHT;
        } else if (angle < 5 * Math.PI / 3) {
            //            System.out.println("rawr");
            direction = branchDirection.MIDDLE;
        } else {
            direction = branchDirection.LEFT;
        }
        return direction;
    }

    /**
     * returns whether or not a branch exists at the given x and y, in the given direction
     *
     * @param xArg      x coordinate of the queried node
     * @param yArg      y coordinate of the queried node
     * @param direction direction of the queried node
     * @return boolean value of whether or not a branch exists here
     */
    public boolean branchExists(int xArg, int yArg, branchDirection direction) {
        try {
            return plantGrid[xArg][yArg].hasBranchInDirection(direction);
        } catch (ArrayIndexOutOfBoundsException ign) {
            return false;
        }
    }

    /**
     * Returns whether or not a node can be grown at the given index
     *
     * @param xIndex x Index of the checked node
     * @param yIndex y Index of the checked node
     * @return if the checked node can be grown at
     */
    public boolean canGrowAtIndex(int xIndex, int yIndex) {

        boolean lowerNode = xIndex % 2 == 0;
        //If this is a node at the base of the plant, return true
        if (yIndex == 0 && lowerNode) return true;
        int yOff = 0;
        if (lowerNode) yOff = 1;

        boolean below = false;
        if (inBounds(xIndex, yIndex - 1))
            below = plantGrid[xIndex][yIndex - 1].hasBranchInDirection(
                    branchDirection.MIDDLE);
        boolean downLeft = false;
        if (inBounds(xIndex - 1, yIndex - yOff))
            downLeft = plantGrid[xIndex - 1][yIndex -
                    yOff].hasBranchInDirection(branchDirection.RIGHT);
        boolean downRight = false;
        if (inBounds(xIndex + 1, yIndex - yOff))
            downRight = plantGrid[xIndex + 1][yIndex -
                    yOff].hasBranchInDirection(branchDirection.LEFT);
        //System.out.println("Below: " + below + " downLeft: " + downLeft + " downRight: " + downRight);
        return below || downLeft || downRight;
    }

    public boolean hasLeaf(float x, float y) {
        int xIndex = screenCoordToIndex(x, y)[0];
        int yIndex = screenCoordToIndex(x, y)[1];
        if (!inBounds(xIndex, yIndex)) return false;
        return plantGrid[xIndex][yIndex].hasLeaf();
    }

    /**
     * upgrades the leaf at the target node
     *
     * @param x         screen x coord of the target node
     * @param y         screem y coord of the target node
     * @param direction direction of the target branch to upgrade
     * @param type      type of branch to upgrade to
     * @return the new branch
     */

    public Branch upgradeBranch(float x,
                                float y,
                                branchDirection direction,
                                Branch.branchType type) {
        int xIndex = screenCoordToIndex(x * worldToPixelConversionRatio,
                                        y * worldToPixelConversionRatio)[0];
        int yIndex = screenCoordToIndex(x * worldToPixelConversionRatio,
                                        y * worldToPixelConversionRatio)[1];
        if (resourceController.canUpgrade()) {
            resourceController.decrementUpgrade();
            plantGrid[xIndex][yIndex].unmakeBranch(direction);
            return plantGrid[xIndex][yIndex].makeBranch(direction, type, world);
        }
        return null;
    }

    /**
     * upgrades the leaf at the target node if there is already one; otherwise makes a normal leaf
     *
     * @param x  screen x coord of the target node
     * @param y  screen y coord of the target node
     * @param lt type of Leaf to upgrade to
     * @return the new Leaf object
     */
    public Leaf makeLeaf(float x, float y, Leaf.leafType lt, float width) {
        int xIndex = screenCoordToIndex(x, y)[0];
        int yIndex = screenCoordToIndex(x, y)[1];
        System.out.println(xIndex + " " + yIndex);
        if (!inBounds(xIndex, yIndex)) return null;
        if (plantGrid[xIndex][yIndex].hasLeaf() &&
                plantGrid[xIndex][yIndex].getLeafType() !=
                        Leaf.leafType.BOUNCY &&
                resourceController.canUpgrade()) {
            plantGrid[xIndex][yIndex].unmakeLeaf();
            resourceController.decrementUpgrade();
            Leaf l = plantGrid[xIndex][yIndex].makeLeaf(Leaf.leafType.BOUNCY, width);
            if (l != null && l.getY() > getMaxLeafHeight()) {
                maxLeafIndex.set(xIndex, yIndex);
            }
            return l;
        } else {
            return growLeaf(x, y, lt, width);
        }
    }

    public float getMaxLeafHeight() {
        if (getMaxLeafXIndex() != -1 &&
                inBounds(getMaxLeafXIndex(), getMaxLeafYIndex()) &&
                plantGrid[getMaxLeafXIndex()][getMaxLeafYIndex()].hasLeaf()) {
            return plantGrid[getMaxLeafXIndex()][getMaxLeafYIndex()].getLeaf()
                    .getY();
        }
        return 0;
    }

    /**
     * grow a leaf at the specified node
     *
     * @param x    screen x coord of the target node
     * @param y    screen y coord of the target node
     * @param type type of leaf to grow
     * @return the grown leaf object
     */

    public Leaf growLeaf(float x, float y, Leaf.leafType type, float width) {
        int xIndex = screenCoordToIndex(x, y)[0];
        int yIndex = screenCoordToIndex(x, y)[1];
        boolean lowerNode = xIndex % 2 == 0;
        if (!inBounds(xIndex, yIndex)) return null;

        if (!plantGrid[xIndex][yIndex].hasLeaf() &&
                (yIndex > 0 || !lowerNode) &&
                resourceController.canGrowLeaf()) {
            Leaf l = plantGrid[xIndex][yIndex].makeLeaf(type, width);
            if (l != null && l.getY() > getMaxLeafHeight()) {
                maxLeafIndex.set(xIndex, yIndex);
            }
            return l;
        }
        return null;
    }

    public int getMaxLeafXIndex() {
        return (int) maxLeafIndex.x;
    }

    public int getMaxLeafYIndex() {
        return (int) maxLeafIndex.y;
    }

    /**
     * destroys a branch at the desired position
     *
     * @param x         the x coordinate of the node to destroy the branch at
     * @param y         the y coordinate of the node to destroy the branch at
     * @param direction the direction in which to destroy the branch
     */
    public void destroyBranch(int x, int y, branchDirection direction) {
        if (plantGrid[x][y].hasBranchInDirection(direction)) {
            plantGrid[x][y].unmakeBranch(direction);
            plantCoyoteTimeRemaining = plantCoyoteTime;
            destructionQueue.addLast(new int[]{x, y});
        }
    }

    /**
     * method to destroy branches no longer attatched to the plant,
     * should be called every frame
     */
    public void propagateDestruction() {
        if (plantCoyoteTimeRemaining == 0 && !destructionQueue.isEmpty()) {
            //System.out.println("Destruction propagation hit");
            int[] currentNode = destructionQueue.removeFirst();
            //System.out.println("CurrentNode: " + currentNode[0] + ", " + currentNode[1]);

            int xIndex = currentNode[0];
            int yIndex = currentNode[1];
            boolean lowerNode = xIndex % 2 == 0;
            for (int i = -1; i < 2; i++) {
                int yOff = 0;
                if (!lowerNode || i == 0) yOff = 1;
                //System.out.println("Checking node x: " + (xIndex + i) + " y: " + (yIndex + yOff));
                //System.out.println("i = " + i);
                //System.out.println("yIndex < height: " + (yIndex+ yOff < height));
                //System.out.println("xIndex + i >= 0: " + (xIndex + i >= 0));
                //System.out.println("xIndex + i < width: " + (xIndex + i < width));
                //System.out.println("Can't grow at node: " + (!canGrowAtIndex(xIndex + i, yIndex+ yOff)));
                //System.out.println("Target node not empty: " + (plantGrid[xIndex + i][yIndex+ yOff].hasBranch() || plantGrid[xIndex + i][yIndex+ yOff].hasLeaf()));
                if (yIndex + yOff < height && xIndex + i >= 0 &&
                        xIndex + i < width &&
                        !canGrowAtIndex(xIndex + i, yIndex + yOff) &&
                        (plantGrid[xIndex + i][yIndex + yOff].hasBranch() ||
                                plantGrid[xIndex + i][yIndex +
                                        yOff].hasLeaf())) {
                    //System.out.println("Floating branch found, i = " + i);
                    destroyAll(xIndex + i, yIndex + yOff);
                    //destructionQueue.addLast(new int[]{xIndex + i, yIndex});
                }
            }
            plantCoyoteTimeRemaining = plantCoyoteTime;
        } else {
            plantCoyoteTimeRemaining--;
        }
    }

    /**
     * destroys all branches and leaves attatched to specified node
     *
     * @param xArg x index of the node to be accessed
     * @param yArg y index of the node to be accessed
     */
    public void destroyAll(int xArg, int yArg) {
        PlantNode nodeToDestroy = plantGrid[xArg][yArg];
        nodeToDestroy.unmakeLeaf();
        if (!canGrowAtIndex(xArg, yArg) ||
                nodeToDestroy.getBranchType(branchDirection.LEFT) ==
                        Branch.branchType.NORMAL)
            nodeToDestroy.unmakeBranch(branchDirection.LEFT);
        else nodeToDestroy.setBranchType(branchDirection.LEFT,
                                         Branch.branchType.NORMAL);
        if (!canGrowAtIndex(xArg, yArg) ||
                nodeToDestroy.getBranchType(branchDirection.MIDDLE) ==
                        Branch.branchType.NORMAL)
            nodeToDestroy.unmakeBranch(branchDirection.MIDDLE);
        else nodeToDestroy.setBranchType(branchDirection.MIDDLE,
                                         Branch.branchType.NORMAL);
        if (!canGrowAtIndex(xArg, yArg) ||
                nodeToDestroy.getBranchType(branchDirection.RIGHT) ==
                        Branch.branchType.NORMAL)
            nodeToDestroy.unmakeBranch(branchDirection.RIGHT);
        else nodeToDestroy.setBranchType(branchDirection.RIGHT,
                                         Branch.branchType.NORMAL);
        calculateMaxLeafIndex();
        plantCoyoteTimeRemaining = plantCoyoteTime;
        destructionQueue.addLast(new int[]{xArg, yArg});
    }

    public void calculateMaxLeafIndex() {
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 1; x < width; x += 2) {
                if (plantGrid[x][y].hasLeaf()) {
                    maxLeafIndex.set(x, y);
                    return;
                }
            }
            for (int x = 0; x < width; x += 2) {
                if (plantGrid[x][y].hasLeaf()) {
                    maxLeafIndex.set(x, y);
                    return;
                }
            }
        }
        maxLeafIndex.set(-1, -1);
    }

    public boolean leafGrowableAt(float xArg, float yArg) {
        return canGrowAt(xArg, yArg) && resourceController.canGrowLeaf();
    }

    /**
     * Returns whether or not a node can be grown at the given world units
     *
     * @param xArg x coordinate of the node in world units
     * @param yArg y coordinate of the node in world units
     * @return whether or not a node can be grown at
     */
    public boolean canGrowAt(float xArg, float yArg) {
        int xIndex = worldCoordToIndex(xArg, yArg)[0];
        int yIndex = worldCoordToIndex(xArg, yArg)[1];
        return canGrowAtIndex(xIndex, yIndex);
    }

    public boolean branchGrowableAt(float xArg,
                                    float yArg,
                                    branchDirection dir) {
        int xIndex = worldCoordToIndex(xArg, yArg)[0];
        int yIndex = worldCoordToIndex(xArg, yArg)[1];
        if (!inBounds(xIndex, yIndex)) return false;
        if (xIndex == 0 && dir != branchDirection.RIGHT) return false;
        if (xIndex == plantGrid.length - 1 && dir != branchDirection.LEFT)
            return false;
        return branchGrowableAt(xArg, yArg) &&
                !plantGrid[xIndex][yIndex].hasBranchInDirection(dir);
    }

    public boolean branchGrowableAt(float xArg, float yArg) {
        return canGrowAt(xArg, yArg) && resourceController.canGrowBranch();
    }

    /**
     * draws the branch that the mouse hovers over
     *
     * @param canvas the canvas to draw to
     */
    public void drawGhostBranch(GameCanvas canvas, float x, float y) {
        int xIndex = worldCoordToIndex(x, y)[0];
        int yIndex = worldCoordToIndex(x, y)[1];
        branchDirection direction = worldToBranch(x, y);
        if (direction != null) {
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
            Branch branch = new Branch(plantGrid[xIndex][yIndex].getX(),
                                       plantGrid[xIndex][yIndex].getY(),
                                       angle,
                                       Branch.branchType.NORMAL,
                                       tilemap,
                                       1);
            branch.setFilmStrip(staticBranchTexture);
            branch.drawGhost(canvas);
        }
    }

    //    /**
    //     * draws the current plant to the canvas
    //     *
    //     * @param canvas the canvas to draw to
    //     */
    //    public void draw(GameCanvas canvas) {
    //        for (PlantNode[] n : plantGrid) {
    //            for (PlantNode node : n) {
    //                try {
    //                    node.drawBranches(canvas);
    //                    node.drawLeaf(canvas);
    //                } catch (Exception ignore) {
    //                    System.out.println("could not draw " + ignore);
    //                }
    //            }
    //        }
    //    }

    /**
     * returns the number of nodes wide the plant is
     */
    public int getWidth() {
        return width;
    }

    /**
     * returns the number of nodes tall the plant is
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the ResourceController
     */
    public ResourceController getResourceController() {
        return resourceController;
    }

    /**
     * sets the texture to use when drawing nodes
     */
    public void gatherAssets(AssetDirectory directory) {

        nodeTexture = directory.getEntry("gameplay:node", Texture.class);
        branchTexture = new FilmStrip(directory.getEntry("gameplay:branch",
                                                         Texture.class),
                                      1,
                                      5,
                                      5);
        staticBranchTexture = new FilmStrip(directory.getEntry("gameplay:branch",
                                                               Texture.class),
                                            1,
                                            5,
                                            5);
        staticBranchTexture.setFrame(4);
        leafTexture = new FilmStrip(directory.getEntry("gameplay:leaf",
                                                       Texture.class), 1, 9, 9);
        leafTextureOne = new FilmStrip(directory.getEntry("gameplay:leaf1",
                Texture.class), 1, 9, 9);
        leafTextureTwo = new FilmStrip(directory.getEntry("gameplay:leaf2",
                Texture.class), 1, 9, 9);
        bouncyLeafTexture = directory.getEntry("gameplay:bouncy",
                                               Texture.class);
        enBranchTextureUp = directory.getEntry("gameplay:enbranch",
                                               Texture.class);
    }

    /**
     * returns if the node at the specified x and y has no branches or leaf
     *
     * @param xArg x coord of the node to be accessed
     * @param yArg y coord of the node to be accessed
     * @return if the node has no branches or leaf
     */
    public boolean nodeIsEmpty(int xArg, int yArg) {
        return plantGrid[xArg][yArg].isEmpty();
    }

    //    /**
    //     * Converts grid indices to screen coordinates.
    //     *
    //     * @param xIndex The x index in the grid.
    //     * @param yIndex The y index in the grid.
    //     * @return A Vector2 object representing the screen coordinates.
    //     */
    //    public Vector2 indexToScreenCoord(int xIndex, int yIndex) {
    //        float screenX = xOrigin + xIndex * xSpacing;
    //        float screenY = yOrigin + yIndex * gridSpacing;
    //        screenY += gridSpacing * .5f * (xIndex % 2);
    //        return new Vector2(screenX, screenY);
    //    }

    /**
     * returns type of branch at given indicies and direction
     *
     * @param xArg x index of the branch to check
     * @param yArg y index of the branch to check
     * @return the type of the checked branch
     */
    public Branch.branchType getBranchType(int xArg,
                                           int yArg,
                                           branchDirection direction) {
        return plantGrid[xArg][yArg].getBranchType(direction);
    }

    /**
     * returns type of leaf at given indicies
     *
     * @param xArg x index of the branch to check
     * @param yArg y index of the branch to check
     * @return the type of the checked branch
     */
    public Leaf.leafType getLeafType(int xArg, int yArg) {
        return plantGrid[xArg][yArg].getLeafType();
    }

    /**
     * enum containing directions in which a plant can grow from a node
     */
    public enum branchDirection {LEFT, MIDDLE, RIGHT}

    /**
     * representation of a node in the plantGrid
     */
    public class PlantNode {

        /**
         * x coordinate of this node
         */
        private final float x;
        /**
         * y coordinate of this node
         */
        private final float y;
        /**
         * width of the leaf at this node
         */
        private float leafWidth = 1.5f;
        /**
         * height of the leaf at this node
         */
        private final float leafHeight = 0.05f;
        /**
         * conversion ration for converting between world coords and pixels
         */
        private final float worldToPixelConversionRatio;
        private final Tilemap tilemap;
        /**
         * whether there is a branch in the leftmost slot of this node
         */
        private Branch left;
        /**
         * whether there is a branch in the middle slot of this node
         */
        private Branch middle;
        /**
         * whether there is a branch in the rightmost slot of this node
         */
        private Branch right;
        /**
         * the leaf for this node
         */
        private Leaf leaf;

        /**
         * initialize a new PlantNode object
         *
         * @param x x coordinate of the node
         * @param y y coordinate of the node
         */
        public PlantNode(float x,
                         float y,
                         float worldToPixelConversionRatio,
                         Tilemap tm) {
            this.x = x;
            this.y = y;
            this.worldToPixelConversionRatio = worldToPixelConversionRatio;
            tilemap = tm;
        }

        /**
         * make a branch in the desired direction, of the desired type
         *
         * @param direction direction branch is facing
         * @param type      type of branch to create
         *                  //         * @param texture   texture the branch should use
         * @param world     world to assign the branch to
         */
        public Branch makeBranch(branchDirection direction,
                                 Branch.branchType type,
                                 World world) {
            float pi = (float) Math.PI;
            Branch newBranch = null;
            switch (direction) {
                case LEFT:
                    left = new Branch(x, y, pi / 3, type, tilemap, 1);
                    newBranch = left;
                    break;
                case MIDDLE:
                    middle = new Branch(x, y, 0, type, tilemap, 1);
                    newBranch = middle;
                    break;
                case RIGHT:
                    right = new Branch(x, y, -pi / 3, type, tilemap, 1);
                    newBranch = right;
                    break;
            }
            if (newBranch != null) {
                switch (type) {
                    case NORMAL:
                        newBranch.setFilmStrip(branchTexture);
                        break;
                    case REINFORCED:
                        newBranch.setTexture(enBranchTextureUp);
                        break;
                }
            }

            resourceController.decrementGrowBranch();
            return newBranch;

        }

        /**
         * make a leaf of the desired type
         *
         * @param type type of leaf to create
         */
        public Leaf makeLeaf(Leaf.leafType type, float width) {
            if (screenCoordToIndex(x / worldToPixelConversionRatio,
                                   y / worldToPixelConversionRatio)[1] > 0 &&
                    !hasLeaf() && hasBranch() ||
                    leafGrowableAt(x / worldToPixelConversionRatio,
                                   y / worldToPixelConversionRatio)) {
                if(type == Leaf.leafType.BOUNCY) width = leafWidth;
                leaf = new Leaf(x / worldToPixelConversionRatio,
                                y / worldToPixelConversionRatio,
                                width,
                                leafHeight,
                                type,
                                tilemap,
                                0.75f);
                switch (type) {
                    case NORMAL:
                        leaf.setFilmStrip(leafTexture);
                        break;
                    case BOUNCY:
                        leaf.setTexture(bouncyLeafTexture);
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
            return null;
        }

        public boolean hasLeaf() {
            return leaf != null;
        }

        public boolean hasBranch() {
            return hasBranchInDirection(branchDirection.LEFT) ||
                    hasBranchInDirection(branchDirection.RIGHT) ||
                    hasBranchInDirection(branchDirection.MIDDLE);
        }

        /**
         * if this node has a branch in the given direction
         *
         * @param direction the direction to check for a branch in
         * @return whether the branch in this direction is not destroyed
         */
        public boolean hasBranchInDirection(branchDirection direction) {
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
         * destroy target branch
         */
        public void unmakeBranch(branchDirection direction) {
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

        public void unmakeLeaf() {
            if (leaf != null) {
                leaf.markRemoved(true);
                leaf = null;
            }
        }

        /**
         * get the branch at a given direction
         *
         * @param direction whether getBranch returns the left, middle, or right branch
         */
        public Branch getBranch(branchDirection direction) {
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
         * returns the leaf of this node
         *
         * @return the leaf of this node
         */
        public Leaf getLeaf() {
            return leaf;
        }

        /**
         * returns the x position of this node
         */
        public float getX() {
            return x;
        }

        /**
         * returns the y position of this node
         */
        public float getY() {
            return y;
        }

        public void reset() {
            if (middle != null) {
                middle.markRemoved(true);
            }
            if (left != null) {
                left.markRemoved(true);
            }
            if (right != null) {
                right.markRemoved(true);
            }
            middle = null;
            left = null;
            right = null;
            leaf = null;
        }

        /**
         * returns whether this branch is empty or not
         *
         * @return if the branch is empty
         */
        public boolean isEmpty() {
            return !(hasBranchInDirection(branchDirection.LEFT) ||
                    hasBranchInDirection(branchDirection.RIGHT) ||
                    hasBranchInDirection(branchDirection.MIDDLE) || hasLeaf());
        }

        /**
         * returns the type of branch in the given direction
         *
         * @param direction the slot to check
         * @return the type of branch in the given slot
         */
        public Branch.branchType getBranchType(branchDirection direction) {
            switch (direction) {
                case LEFT:
                    if (hasBranchInDirection(branchDirection.LEFT))
                        return left.getBranchType();
                    else return null;
                case RIGHT:
                    if (hasBranchInDirection(branchDirection.RIGHT))
                        return right.getBranchType();
                    else return null;
                case MIDDLE:
                    if (hasBranchInDirection(branchDirection.MIDDLE))
                        return middle.getBranchType();
                    else return null;
            }
            //There is no branch at this node
            return null;
        }

        /**
         * sets the type of the branch in the given direction and resets its texture
         *
         * @param direction the slot to set
         * @param btype     the branch type to set the given slot to
         */
        public void setBranchType(branchDirection direction,
                                  Branch.branchType btype) {
            switch (direction) {
                case LEFT:
                    if (hasBranchInDirection(branchDirection.LEFT)) {
                        left.setBranchType(btype);
                        left.setFilmStrip(branchTexture);
                    }
                    break;
                case RIGHT:
                    if (hasBranchInDirection(branchDirection.RIGHT)) {
                        right.setBranchType(btype);
                        right.setFilmStrip(branchTexture);
                    }
                    break;
                case MIDDLE:
                    if (hasBranchInDirection(branchDirection.MIDDLE)) {
                        middle.setBranchType(btype);
                        middle.setFilmStrip(branchTexture);
                    }
                    break;
            }
        }

        /**
         * returns the type of leaf at this node, null if no leaf
         *
         * @return
         */
        public Leaf.leafType getLeafType() {
            if (hasLeaf()) return leaf.getLeafType();
            else return null;
        }

    }

}
