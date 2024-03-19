package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Queue;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.WorldController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Branch;
import com.syndic8.phytopolis.level.models.Leaf;

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
    private final float worldToPixelConversionRatio = 120;
    /**
     * how many frames between propagations of destruction
     */
    private final int plantCoyoteTime = 30;
    private final Queue<int[]> destructionQueue = new Queue<>();
    /**
     * node texture
     */
    protected Texture nodeTexture;
    /**
     * branch texture
     */
    protected Texture branchTexture;
    /**
     * leaf texture
     */
    protected Texture leafTexture;
    /**
     * how many more frames until the next propagation of destruction
     */
    private int plantCoyoteTimeRemaining = 0;

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
                           Vector2 scale) {
        plantGrid = new PlantNode[width][height];
        this.world = world;
        this.xOrigin = xOrigin * worldToPixelConversionRatio;
        this.yOrigin = yOrigin * worldToPixelConversionRatio;
        this.width = width;
        this.height = height;
        this.gridSpacing = gridSpacing * worldToPixelConversionRatio;
        this.xSpacing = (float) (Math.sqrt(
                (this.gridSpacing * this.gridSpacing) -
                        ((this.gridSpacing / 2f) * (this.gridSpacing / 2f))));
        this.scale = scale;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float yOffset = 0;
                //set the yOffset
                if (x % 2 == 1) yOffset = this.gridSpacing / 2f;
                plantGrid[x][y] = new PlantNode(
                        (x * this.xSpacing) + this.xOrigin,
                        this.yOrigin + yOffset + (y * this.gridSpacing),
                        this.worldToPixelConversionRatio);
            }
        }
    }

    /**
     * grows a branch at the desired position
     *
     * @param x         the x coordinate of the node to grow the branch at
     * @param y         the y coordinate of the node to grow the branch at
     * @param direction the direction in which to grow the branch
     * @param type      the type of branch to grow
     */
    public void growBranch(float x,
                           float y,
                           branchDirection direction,
                           branchType type) {
        int xIndex = xCoordToIndex(x * worldToPixelConversionRatio);
        int yIndex = yCoordToIndex(y * worldToPixelConversionRatio);
        plantGrid[xIndex][yIndex].makeBranch(direction,
                                             type,
                                             branchTexture,
                                             world);
    }

    public void growLeaf(float x,
                         float y,
                         leafType type,
                         WorldController wldc) {
        int xIndex = xCoordToIndex(x);
        int yIndex = yCoordToIndex(y);
        plantGrid[xIndex][yIndex].makeLeaf(type, leafTexture, world, wldc);
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
     * destroys all branches and leaves attatched to specified node
     *
     * @param xArg x index of the node to be accessed
     * @param yArg y index of the node to be accessed
     */
    public void destroyAll(int xArg, int yArg) {
        PlantNode nodeToDestroy = plantGrid[xArg][yArg];
        nodeToDestroy.unmakeLeaf();
        nodeToDestroy.unmakeBranch(branchDirection.LEFT);
        nodeToDestroy.unmakeBranch(branchDirection.MIDDLE);
        nodeToDestroy.unmakeBranch(branchDirection.RIGHT);
        plantCoyoteTimeRemaining = plantCoyoteTime;
        destructionQueue.addLast(new int[]{xArg, yArg});
    }

    /**
     * method to destroy branches no longer attatched to the plant,
     * should be called every frame
     */
    public void propagateDestruction() {
        if (plantCoyoteTimeRemaining == 0 && !destructionQueue.isEmpty()) {
            int[] currentNode = destructionQueue.removeFirst();
            int xIndex = currentNode[0];
            int yIndex = currentNode[1] + 1;
            for (int i = -1; i < 2; i++) {
                if (yIndex < height && xIndex >= 0 && xIndex < width &&
                        !canGrowAt(xIndex + i, yIndex) && !plantGrid[xIndex + i][yIndex].isEmpty()) {
                    destroyAll(xIndex + i, yIndex);
                    destructionQueue.addLast(new int[]{xIndex + i, yIndex});
                }
            }
            plantCoyoteTimeRemaining = plantCoyoteTime;
        } else {
            plantCoyoteTimeRemaining--;
        }
    }

    /**
     * Returns whether or not a node can be grown at
     *
     * @param xArg x coordinate of the node in world units
     * @param yArg y coordinate of the node in world units
     * @return whether or not a node can be grown at
     */
    public boolean canGrowAt(float xArg, float yArg) {
        int xIndex = xCoordToIndex(xArg * worldToPixelConversionRatio);
        int yIndex = yCoordToIndex(yArg * worldToPixelConversionRatio);
        boolean lowerNode = xIndex % 2 == 0;
        //If this is a node at the base of the plant, return true
        if (yIndex == 0 && lowerNode) return true;
        int yOff = 0;
        if (lowerNode) yOff = 1;

        boolean below = false;
        if (yIndex > 0)
            below = plantGrid[xIndex][yIndex - 1].hasBranchInDirection(
                    branchDirection.MIDDLE);
        boolean downLeft = false;
        if (xIndex >= 1) downLeft = plantGrid[xIndex - 1][yIndex -
                yOff].hasBranchInDirection(branchDirection.RIGHT);
        boolean downRight = false;
        if (xIndex < width - 1) downRight = plantGrid[xIndex + 1][yIndex -
                yOff].hasBranchInDirection(branchDirection.LEFT);
        //System.out.println("Below: " + below + " downLeft: " + downLeft + " downRight: " + downRight);
        return below || downLeft || downRight;
    }

    /**
     * draws the current plant to the canvas
     *
     * @param canvas the canvas to draw to
     */
    public void draw(GameCanvas canvas) {
        try {
            for (PlantNode[] n : plantGrid) {
                for (PlantNode node : n) {
                    //                    canvas.draw(nodeTexture,
                    //                                Color.WHITE,
                    //                                nodeTexture.getWidth() / 2f,
                    //                                nodeTexture.getHeight() / 2f,
                    //                                node.getX(),
                    //                                node.getY(),
                    //                                0.0f,
                    //                                1.0f,
                    //                                1.0f);
                    try {
                        node.drawBranches(canvas);
                        node.drawLeaf(canvas);
                    } catch (Exception ignore) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }

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
     * sets the texture to use when drawing nodes
     */
    public void gatherAssets(AssetDirectory directory) {

        this.nodeTexture = directory.getEntry("gameplay:node", Texture.class);
        this.branchTexture = directory.getEntry("gameplay:branch",
                                                Texture.class);
        this.leafTexture = directory.getEntry("gameplay:leaf", Texture.class);
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
        return plantGrid[xArg][yArg].hasBranchInDirection(direction);
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

    /**
     * Converts a screen x coordinate to the corresponding node index in the plant
     *
     * @param xArg x coordinate to be converted
     * @return x index of the corresponding node
     */
    public int xCoordToIndex(float xArg) {
        return Math.round((xArg - xOrigin) / xSpacing);
    }

    /**
     * Converts a screen y coordinate to the corresponding node index in the plant
     *
     * @param yArg y coordinate to be converted
     * @return y index of the corresponding node
     */
    public int yCoordToIndex(float yArg) {
        return (int) ((yArg - yOrigin) / gridSpacing);
    }

    /**
     * Convert x world coord to an index in PlantController
     *
     * @param xArg x world coordinate
     * @return the corresponding index
     */
    public int xWorldCoordToIndex(float xArg) {
        return xCoordToIndex(xArg * worldToPixelConversionRatio);
    }

    /**
     * Convert y world coord to an index in PlantController
     *
     * @param yArg y world coordinate
     * @return the corresponding index
     */
    public int yWorldCoordToIndex(float yArg) {
        return yCoordToIndex(yArg * worldToPixelConversionRatio);
    }

    /**
     * returns the world coordinates of the node at the specified index
     * @param xArg x index
     * @param yArg y index
     * @return vector of world coordinates
     */
    public Vector2 indexToWorldCoord(int xArg, int yArg){
        PlantNode n = plantGrid[xArg][yArg];
        return new Vector2(n.x / worldToPixelConversionRatio, n.y / worldToPixelConversionRatio);
    }

    //TODO: Update with new branch type

    /**
     * enum containing directions in which a plant can grow from a node
     */
    public enum branchDirection {LEFT, MIDDLE, RIGHT}

    /**
     * enum containing possible branch types
     */
    public enum branchType {NORMAL}

    /**
     * enum containing possible leaf types
     */
    public enum leafType {NORMAL}

    /**
     * representation of a node in the plantGrid
     */
    public class PlantNode {
        //TODO: change this class to contain branch objects instead of booleans
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
        private final float leafWidth = 1;
        /**
         * height of the leaf at this node
         */
        private final float leafHeight = 0.25f;
        /**
         * conversion ration for converting between world coords and pixels
         */
        private final float worldToPixelConversionRatio;
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
         * if a leaf exists at this node
         */
        private boolean leafExists = false;

        /**
         * initialize a new PlantNode object
         *
         * @param x x coordinate of the node
         * @param y y coordinate of the node
         */
        public PlantNode(float x, float y, float worldToPixelConversionRatio) {
            //            this.left = false;
            //            this.middle = false;
            //            this.right = false;
            this.x = x;
            this.y = y;
            this.worldToPixelConversionRatio = worldToPixelConversionRatio;
        }

        /**
         * make a branch in the desired direction, of the desired type
         *
         * @param direction direction branch is facing
         * @param type      type of branch to create
         * @param texture   texture the branch should use
         * @param world     world to assign the branch to
         */
        public void makeBranch(branchDirection direction,
                               branchType type,
                               Texture texture,
                               World world) {
            float pi = (float) Math.PI;
            switch (direction) {
                case LEFT:
                    left = new Branch(x, y, pi / 3);
                    left.setTexture(branchTexture);
                    break;
                case MIDDLE:
                    middle = new Branch(x, y, 0);
                    middle.setTexture(branchTexture);
                    break;
                case RIGHT:
                    right = new Branch(x, y, -pi / 3);
                    right.setTexture(branchTexture);
                    break;
            }

        }

        /**
         * make a leaf of the desired type
         *
         * @param type    type of leaf to create
         * @param texture texture of the leaf
         * @param world   world to assign the leaf to
         */
        public void makeLeaf(leafType type,
                             Texture texture,
                             World world,
                             WorldController wldc) {
            if (!leafExists) {
                leaf = new Leaf(x / worldToPixelConversionRatio,
                                y / worldToPixelConversionRatio,
                                leafWidth,
                                leafHeight);
                leaf.setTexture(texture);
                leaf.setDrawScale(10, 10);
                leafExists = true;
                wldc.addObject(leaf);
            }
        }

        //TODO: less hacky unmakeBranch implementation

        /**
         * destroy target branch
         */
        public void unmakeBranch(branchDirection direction) {
            switch (direction) {
                case MIDDLE:
                    //middle.setDestroyed(true);
                    middle = null;
                case RIGHT:
                    //right.setDestroyed(true);
                    right = null;
                case LEFT:
                    //left.setDestroyed(true);
                    left = null;
            }
        }

        public void unmakeLeaf() {
            leafExists = false;
            leaf = null;
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

        public void drawBranches(GameCanvas canvas) {
            if (left != null) left.draw(canvas);
            if (right != null) right.draw(canvas);
            if (middle != null) middle.draw(canvas);
        }

        public void drawLeaf(GameCanvas canvas) {
            if (leaf != null) {
                leaf.draw(canvas);
            }
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
                    return middle != null && !middle.isDestroyed();
                case LEFT:
                    return left != null && !left.isDestroyed();
                case RIGHT:
                    return right != null && !right.isDestroyed();
                default:
                    return false;
            }
        }

        public boolean hasLeaf() {
            return leafExists;
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

    }

}
