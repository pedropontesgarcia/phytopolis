package com.syndic8.phytopolis.level;

import com.badlogic.gdx.Gdx;
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
                           ResourceController rc) {
        plantGrid = new PlantNode[width][height];
        this.world = world;
        worldToPixelConversionRatio = scale.x;
        this.scale = scale;
        this.xOrigin = xOrigin * worldToPixelConversionRatio;
        this.yOrigin = yOrigin * worldToPixelConversionRatio;
        this.width = width;
        this.height = height;
        this.gridSpacing = gridSpacing * worldToPixelConversionRatio;
        this.xSpacing = (float) (Math.sqrt(
                (this.gridSpacing * this.gridSpacing) -
                        ((this.gridSpacing / 2f) * (this.gridSpacing / 2f))));
        this.resourceController = rc;
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
                           Branch.branchType type) {
        int xIndex = screenCoordToIndex(x * worldToPixelConversionRatio, y * worldToPixelConversionRatio)[0];
        int yIndex = screenCoordToIndex(x * worldToPixelConversionRatio, y * worldToPixelConversionRatio)[1];
        plantGrid[xIndex][yIndex].makeBranch(direction, type, world);
    }

    public Leaf growLeaf(float x,
                         float y,
                         Leaf.leafType type) {
        int xIndex = screenCoordToIndex(x, y)[0];
        int yIndex = screenCoordToIndex(x, y)[1];
        boolean lowerNode = xIndex % 2 == 0;
        if(!plantGrid[xIndex][yIndex].hasLeaf() && (yIndex>0 || !lowerNode)) return plantGrid[xIndex][yIndex].makeLeaf(type);
        return null;
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
        if(!canGrowAtIndex(xArg, yArg) || nodeToDestroy.getBranchType(branchDirection.LEFT) == Branch.branchType.NORMAL) nodeToDestroy.unmakeBranch(branchDirection.LEFT);
        else nodeToDestroy.setBranchType(branchDirection.LEFT, Branch.branchType.NORMAL);
        if(!canGrowAtIndex(xArg, yArg) || nodeToDestroy.getBranchType(branchDirection.MIDDLE) == Branch.branchType.NORMAL) nodeToDestroy.unmakeBranch(branchDirection.MIDDLE);
        else nodeToDestroy.setBranchType(branchDirection.MIDDLE, Branch.branchType.NORMAL);
        if(!canGrowAtIndex(xArg, yArg) || nodeToDestroy.getBranchType(branchDirection.RIGHT) == Branch.branchType.NORMAL) nodeToDestroy.unmakeBranch(branchDirection.RIGHT);
        else nodeToDestroy.setBranchType(branchDirection.RIGHT, Branch.branchType.NORMAL);
        plantCoyoteTimeRemaining = plantCoyoteTime;
        destructionQueue.addLast(new int[]{xArg, yArg});
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
                if(!lowerNode || i==0) yOff = 1;
                //System.out.println("Checking node x: " + (xIndex + i) + " y: " + (yIndex + yOff));
                //System.out.println("i = " + i);
                //System.out.println("yIndex < height: " + (yIndex+ yOff < height));
                //System.out.println("xIndex + i >= 0: " + (xIndex + i >= 0));
                //System.out.println("xIndex + i < width: " + (xIndex + i < width));
                //System.out.println("Can't grow at node: " + (!canGrowAtIndex(xIndex + i, yIndex+ yOff)));
                //System.out.println("Target node not empty: " + (plantGrid[xIndex + i][yIndex+ yOff].hasBranch() || plantGrid[xIndex + i][yIndex+ yOff].hasLeaf()));
                if (yIndex + yOff < height && xIndex + i >= 0 && xIndex + i < width &&
                        !canGrowAtIndex(xIndex + i, yIndex + yOff) &&
                        (plantGrid[xIndex + i][yIndex+ yOff].hasBranch() || plantGrid[xIndex + i][yIndex+ yOff].hasLeaf())) {
                    //System.out.println("Floating branch found, i = " + i);
                    destroyAll(xIndex + i, yIndex+ yOff);
                    //destructionQueue.addLast(new int[]{xIndex + i, yIndex});
                }
            }
            plantCoyoteTimeRemaining = plantCoyoteTime;
        } else {
            plantCoyoteTimeRemaining--;
        }
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

    /**
     * Returns whether or not a node can be grown at the given index
     * @param xIndex x Index of the checked node
     * @param yIndex y Index of the checked node
     * @return if the checked node can be grown at
     */
    public boolean canGrowAtIndex(int xIndex, int yIndex){

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

    public boolean growableAt(float xArg, float yArg) {
        return canGrowAt(xArg, yArg) && resourceController.canGrow();
    }

    public boolean branchGrowableAt(float xArg,
                                    float yArg,
                                    branchDirection dir) {
        int xIndex = worldCoordToIndex(xArg, yArg)[0];
        int yIndex = worldCoordToIndex(xArg, yArg)[1];
        return growableAt(xArg, yArg) &&
                !plantGrid[xIndex][yIndex].hasBranchInDirection(dir);
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

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
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

        this.nodeTexture = directory.getEntry("gameplay:node", Texture.class);
        this.branchTexture = directory.getEntry("gameplay:branch",
                                                Texture.class);
        this.leafTexture = directory.getEntry("gameplay:leaf", Texture.class);
        this.bouncyLeafTexture = directory.getEntry("gameplay:bouncy", Texture.class);
        this.enBranchTextureUp = directory.getEntry("gameplay:enbranch", Texture.class);
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
     * Converts a screen coordinate to the corresponding node index in the plant
     *
     * @param xArg x coordinate to be converted
     * @param yArg y coordinate to be converted
     * @return (x, y) index of the corresponding node
     */
    public int[] screenCoordToIndex(float xArg, float yArg) {
        int xIndex = Math.round((xArg - xOrigin) / xSpacing);
        int yIndex =  (int) ((yArg - yOrigin - (gridSpacing * .5f * (xIndex % 2))) / gridSpacing);
        return new int[] {xIndex, yIndex};
    }

    /**
     * Converts a world coordinate to the corresponding node index in the plant
     *
     * @param xArg x coordinate to be converted
     * @param yArg y coordinate to be converted
     * @return (x, y) index of the corresponding node
     */
    public int[] worldCoordToIndex(float xArg, float yArg) {
        return screenCoordToIndex(xArg * worldToPixelConversionRatio, yArg * worldToPixelConversionRatio);
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

    /**
     * Converts grid indices to screen coordinates.
     *
     * @param xIndex The x index in the grid.
     * @param yIndex The y index in the grid.
     * @return A Vector2 object representing the screen coordinates.
     */
    public Vector2 indexToScreenCoord(int xIndex, int yIndex) {
        float screenX = xOrigin + xIndex * xSpacing;
        float screenY = yOrigin + yIndex * gridSpacing;
        screenY += gridSpacing * .5f * (xIndex % 2);
        return new Vector2(screenX, screenY);
    }

    /**
     * returns type of branch at given indicies and direction
     * @param xArg x index of the branch to check
     * @param yArg y index of the branch to check
     * @return the type of the checked branch
     */
    public Branch.branchType getBranchType(int xArg, int yArg, branchDirection direction){
        return plantGrid[xArg][yArg].getBranchType(direction);
    }

    /**
     * returns type of leaf at given indicies
     * @param xArg x index of the branch to check
     * @param yArg y index of the branch to check
     * @return the type of the checked branch
     */
    public Leaf.leafType getLeafType(int xArg, int yArg){
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
        private final float leafWidth = 1.5f;
        /**
         * height of the leaf at this node
         */
        private final float leafHeight = 0.05f;
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
         *                  //         * @param texture   texture the branch should use
         * @param world     world to assign the branch to
         */
        public void makeBranch(branchDirection direction,
                               Branch.branchType type,
                               World world) {
            float pi = (float) Math.PI;
            if (branchTexture != null) {
                Branch newBranch = null;
                switch (direction) {
                    case LEFT:
                        left = new Branch(x, y, pi / 3, type);
                        newBranch = left;
                        break;
                    case MIDDLE:
                        middle = new Branch(x, y, 0, type);
                        newBranch = middle;
                        break;
                    case RIGHT:
                        right = new Branch(x, y, -pi / 3, type);
                        newBranch = right;
                        break;
                }
                if (newBranch != null) {
                    switch (type){
                        case NORMAL:
                            newBranch.setTexture(branchTexture);
                            break;
                        case REINFORCED:
                            newBranch.setTexture(enBranchTextureUp);
                            break;
                    }
                }
            }
            resourceController.decrementGrow();

        }

        /**
         * make a leaf of the desired type
         *
         * @param type    type of leaf to create
         */
        public Leaf makeLeaf(Leaf.leafType type) {
            if (screenCoordToIndex(x / worldToPixelConversionRatio, y / worldToPixelConversionRatio)[1] > 0 &&
                    !leafExists && hasBranch() ||
                    growableAt(x / worldToPixelConversionRatio,
                               y / worldToPixelConversionRatio)) {
                leaf = new Leaf(x / worldToPixelConversionRatio,
                                y / worldToPixelConversionRatio, //- 0.5f,
                                leafWidth,
                                leafHeight, type);
                switch (type){
                    case NORMAL:
                        leaf.setTexture(leafTexture);
                        break;
                    case BOUNCY:
                        leaf.setTexture(bouncyLeafTexture);
                        break;
                }
                leaf.setDrawScale(scale.x, scale.y);
                leafExists = true;
                //worldcontroller.addObject(leaf);
                resourceController.decrementGrow();
                return leaf;
            }
            return null;
        }

        /**
         * destroy target branch
         */
        public void unmakeBranch(branchDirection direction) {
            switch (direction) {
                case MIDDLE:
                    //middle.setDestroyed(true);
                    middle = null;
                    break;
                case RIGHT:
                    //right.setDestroyed(true);
                    right = null;
                    break;
                case LEFT:
                    //left.setDestroyed(true);
                    left = null;
                    break;
            }
        }

        public void unmakeLeaf() {
            leafExists = false;
            if (leaf != null) {
                leaf.markRemoved(true);
                //leaf = null;
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

        public void drawBranches(GameCanvas canvas) {
            if (left != null) left.draw(canvas);
            if (right != null) right.draw(canvas);
            if (middle != null) middle.draw(canvas);
        }

        public void drawLeaf(GameCanvas canvas) {
            if (leaf != null && !leaf.isRemoved()) {
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

        public boolean hasBranch() {
            return hasBranchInDirection(branchDirection.LEFT) ||
                    hasBranchInDirection(branchDirection.RIGHT) ||
                    hasBranchInDirection(branchDirection.MIDDLE);
        }

        /**
         * returns the type of branch in the given direction
         * @param direction the slot to check
         * @return the type of branch in the given slot
         */
        public Branch.branchType getBranchType(branchDirection direction){
            switch (direction){
                case LEFT:
                    if (hasBranchInDirection(branchDirection.LEFT)) return left.getBranchType();
                    else return null;
                case RIGHT:
                    if (hasBranchInDirection(branchDirection.RIGHT)) return right.getBranchType();
                    else return null;
                case MIDDLE:
                    if (hasBranchInDirection(branchDirection.MIDDLE)) return middle.getBranchType();
                    else return null;
            }
            //There is no branch at this node
            return null;
        }

        /**
         * sets the type of the branch in the given direction and resets its texture
         * @param direction the slot to set
         * @param btype the branch type to set the given slot to
         */
        public void setBranchType(branchDirection direction, Branch.branchType btype){
            switch (direction){
                case LEFT:
                    if (hasBranchInDirection(branchDirection.LEFT)){
                        left.setBranchType(btype);
                        left.setTexture(branchTexture);
                    }
                    break;
                case RIGHT:
                    if (hasBranchInDirection(branchDirection.RIGHT)){
                        right.setBranchType(btype);
                        right.setTexture(branchTexture);
                    }
                    break;
                case MIDDLE:
                    if (hasBranchInDirection(branchDirection.MIDDLE)){
                        middle.setBranchType(btype);
                        middle.setTexture(branchTexture);
                    }
                    break;
            }
        }

        /**
         * returns the type of leaf at this node, null if no leaf
         * @return
         */
        public Leaf.leafType getLeafType(){
            if(hasLeaf()) return leaf.getLeafType();
            else return null;
        }

    }

}
