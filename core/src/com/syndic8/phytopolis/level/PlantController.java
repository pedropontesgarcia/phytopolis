package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.PolygonObject;

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
     * node texture
     */
    protected Texture nodeTexture;
    /**
     * branch texture
     */
    protected TextureRegion branchTexture;

    /**
     * Initialize a PlantController with specified height and width
     *
     * @param height      the height of the plant grid
     * @param width       the width of the plant grid
     * @param gridSpacing the spacing between nodes of the plant
     * @param world       world to assign physics objects to
     * @param xOrigin     x origin of the plant
     * @param yOrigin     y origin of the plant
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
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.width = width;
        this.height = height;
        this.gridSpacing = gridSpacing;
        this.xSpacing = (float) Math.sqrt((gridSpacing * gridSpacing) -
                ((gridSpacing / 2f) *
                        (gridSpacing / 2f)));
        this.scale = scale;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float yOffset = 0;
                //set the yOffset
                if (x % 2 == 1) yOffset = gridSpacing / 2f;
                plantGrid[x][y] = new PlantNode((x * xSpacing) + xOrigin,
                        yOrigin + yOffset +
                                (y * gridSpacing));
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
    public PolygonObject growBranch(float x,
                                      float y,
                                      branchDirection direction,
                                      branchType type) {
        //execute only if the branch being created won't be out of bounds
        //if ((x > 0 && x < width) ||
        //        (x == 0 && direction != branchDirection.LEFT) ||
        //        (x == width && direction != branchDirection.RIGHT)) {
        //    //if there isn't already a branch at the x and y coordinates
        // provided, create one there
        //    if (plantGrid[x][y].getBranch(direction) == null)
        //        int xIndex = Math.round((x - xOrigin) / width) + 2;
        //        int yIndex = Math.round((y - yOrigin) / gridSpacing);
        int xIndex = xCoordToIndex(x * 10);
        int yIndex = yCoordToIndex(y * 10);
        System.out.println(x);
        System.out.println(y);
        System.out.println(xIndex);
        System.out.println(yIndex);
        return plantGrid[xIndex][yIndex].makeBranch(direction,
                type,
                branchTexture,
                world);
        //}
    }

    /**
     *
     * @param x the x-index of the node to destroy branches from
     * @param y the y-index of the node to destroy branches from
     */
    public void destroyNode(int x, int y) {
        System.out.println("destroy node " + x + " " + y);
        destroyBranch(x, y, PlantController.branchDirection.LEFT);
        destroyBranch(x, y, PlantController.branchDirection.MIDDLE);
        destroyBranch(x, y, PlantController.branchDirection.RIGHT);
    }

    /**
     * destroys a branch at the desired position
     *
     * @param x         the x coordinate of the node to destroy the branch at
     * @param y         the y coordinate of the node to destroy the branch at
     * @param direction the direction in which to destroy the branch
     */
    public void destroyBranch(int x, int y, branchDirection direction) {
//        if (plantGrid[x][y].getBranch(direction) != null)
        System.out.println("destroy branch " + x + " " + y);
        switch(direction) {
            case MIDDLE:
                System.out.println("mid");
                break;
            case LEFT:
                System.out.println("left");
                break;
            case RIGHT:
                System.out.println("right");
                break;
        }
        if (plantGrid[x][y].hasBranchInDirection(direction)) {
            System.out.println("has branch");
            plantGrid[x][y].unmakeBranch(direction);
            switch (direction) {
                case MIDDLE:
                    if (validIndices(x, y+1)) {
                        destroyNode(x, y+1);
                    }
                    break;
                case LEFT:
                    if (validIndices(x-1, y+(x%2==1 ? 1 : 0))) {
                        destroyNode(x-1, y+(x%2==1 ? 1 : 0));
                    }
                    break;
                case RIGHT:
                    if (validIndices(x+1, y+(x%2==1 ? 1 : 0))) {
                        destroyNode(x+1, y+(x%2==1 ? 1 : 0));
                    }
                    break;
            }
        }
    }

    /**
     *
     * @param x the x-index of the node to check if valid
     * @param y the y-index of the node to check if valid
     * @return boolean value of if the given node is valid
     */
    public boolean validIndices(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean canGrowAt(float xArg, float yArg){
        int xIndex = xCoordToIndex(xArg* 10);
        int yIndex = yCoordToIndex(yArg * 10);
        boolean lowerNode = xIndex % 2 == 0;
        if(yIndex == 0 && lowerNode) return true;
        int yOff = 0;
        if(lowerNode) yOff = 1;

        boolean below = false;
        if(yIndex > 0) below = plantGrid[xIndex][yIndex-1].hasBranchInDirection(branchDirection.MIDDLE);
        boolean downLeft = false;
        if(xIndex >= 1) downLeft = plantGrid[xIndex-1][yIndex-yOff].hasBranchInDirection(branchDirection.RIGHT);
        boolean downRight = false;
        if(xIndex < width-1) downRight = plantGrid[xIndex+1][yIndex-yOff].hasBranchInDirection(branchDirection.LEFT);
        System.out.println("Below: " + below + " downLeft: " + downLeft + " downRight: " + downRight);
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
                    canvas.draw(nodeTexture,
                            Color.WHITE,
                            nodeTexture.getWidth() / 2f,
                            nodeTexture.getHeight() / 2f,
                            node.getX(),
                            node.getY(),
                            0.0f,
                            1.0f,
                            1.0f);
                    try {
                        node.drawBranches(canvas);
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

        this.nodeTexture = directory.getEntry("shared:node", Texture.class);
        this.branchTexture = new TextureRegion(directory.getEntry(
                "shared:branch",
                Texture.class));
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
        return plantGrid[xArg][yArg].connectionExists(direction);
    }

    /**
     *
     * @param xIndex x-index of node to check
     * @param yIndex y-index of node to check
     * @return boolean value of if given node has any branches
     */
    public boolean existsBranch(int xIndex, int yIndex) {
        return plantGrid[xIndex][yIndex].hasBranch();
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

    //TODO: Update with new branch type

    /**
     * enum containing directions in which a plant can grow from a node
     */
    public enum branchDirection {LEFT, MIDDLE, RIGHT}

    /**
     * enum containing possible plant types
     */
    public enum branchType {NORMAL}

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
         * the branch grown to the left from this node
         */
        private PolygonObject left;
        /**
         * the branch grown upward from this node
         */
        private PolygonObject middle;
        /**
         * the branch grown to the right from this node
         */
        private PolygonObject right;
        /**
         * whether there is a branch in the leftmost slot of this node
         */
        private boolean hasLeft = false;
        /**
         * whether there is a branch in the middle slot of this node
         */
        private boolean hasMiddle = false;
        /**
         * whether there is a branch in the rightmost slot of this node
         */
        private boolean hasRight = false;

        /**
         * initialize a new PlantNode object
         *
         * @param x x coordinate of the node
         * @param y y coordinate of the node
         */
        public PlantNode(float x, float y) {
            //            this.left = false;
            //            this.middle = false;
            //            this.right = false;
            this.x = x;
            this.y = y;
        }

        /**
         * make a branch in the desired direction, of the desired type
         *
         * @param direction direction branch is facing
         * @param type      type of branch to create
         * @param texture   texture the branch should use
         * @param world     world to assign the branch to
         */
        public PolygonObject makeBranch(branchDirection direction,
                                          branchType type,
                                          TextureRegion texture,
                                          World world) {
            PolygonObject obj;
            switch (direction) {
                case LEFT:
                    //left = new Leaf(world, new Vector2(x, y), texture, -60f);
                    obj = new PolygonObject(new float[]{-0.5f,
                            0,
                            -0.5f,
                            5.5f,
                            0.5f,
                            5.5f,
                            0.5f,
                            0}, x / 10, y / 10);
                    obj.setBodyType(BodyDef.BodyType.StaticBody);
                    obj.setDensity(0);
                    obj.setFriction(0);
                    obj.setRestitution(0);
                    obj.setDrawScale(scale);
                    obj.setTexture(branchTexture);
                    obj.setName("tet");
                    obj.getBody(world).setUserData("tet");
                    obj.setAngle((float) Math.toRadians(60));
                    left = obj;
                    hasLeft = true;
                    return obj;
                case MIDDLE:
                    //middle = new Leaf(world, new Vector2(x, y), texture, 0f);

                    obj = new PolygonObject(new float[]{-0.5f,
                            0,
                            -0.5f,
                            5.5f,
                            0.5f,
                            5.5f,
                            0.5f,
                            0}, x / 10, y / 10);
                    obj.setBodyType(BodyDef.BodyType.StaticBody);
                    obj.setDensity(0);
                    obj.setFriction(0);
                    obj.setRestitution(0);
                    obj.setDrawScale(scale);
                    obj.setTexture(branchTexture);
                    obj.setName("tet");
                    obj.getBody(world).setUserData("tet");
                    obj.setAngle(0);
                    middle = obj;
                    hasMiddle = true;
                    return obj;
                case RIGHT:
                    //right = new Leaf(world, new Vector2(x, y), texture, 60f);

                    obj = new PolygonObject(new float[]{-0.5f,
                            0,
                            -0.5f,
                            5.5f,
                            0.5f,
                            5.5f,
                            0.5f,
                            0}, x / 10, y / 10);
                    obj.setBodyType(BodyDef.BodyType.StaticBody);
                    obj.setDensity(0);
                    obj.setFriction(0);
                    obj.setRestitution(0);
                    obj.setDrawScale(scale);
                    obj.setTexture(branchTexture);
                    obj.setName("tet");
                    obj.getBody(world).setUserData("tet");
                    obj.setAngle((float) Math.toRadians(-60));
                    right = obj;
                    hasRight = true;
                    return obj;
            }
            return null;

        }

        //TODO: implement branch destruction

        /**
         * destroy target branch
         */
        public void unmakeBranch(branchDirection direction) {
            switch (direction) {
                case MIDDLE:
                    System.out.println("unmade mid");
                    middle.markRemoved(true);
                    middle = null;
                    hasMiddle = false;
                    break;
                case LEFT:
                    System.out.println("unmade left");
                    left.markRemoved(true);
                    left = null;
                    hasLeft = false;
                    break;
                case RIGHT:
                    System.out.println("unmade right");
                    right.markRemoved(true);
                    right = null;
                    hasRight = false;
                    break;
                default:
                    System.out.println("Something is wrong");
            }
        }

        //TODO: set return type to branch object instead of boolean

        /**
         * get the branch at a given direction
         *
         * @param direction whether getBranch returns the left, middle, or right branch
         */
        public PolygonObject getBranch(branchDirection direction) {
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

        /**
         *
         * @param canvas Canvas to draw branches to
         */
        public void drawBranches(GameCanvas canvas) {
            if (left != null) {
                left.draw(canvas);
            }
            if (right != null) {
                right.draw(canvas);
            }
            if (middle != null) {
                middle.draw(canvas);
            }
        }

        /**
         *
         * @param direction which direction to check
         * @return boolean value of if current node has branch in given direction based on
         *          object attributes
         */
        public boolean connectionExists(branchDirection direction) {
            switch (direction) {
                case MIDDLE:
                    return middle != null;
                case LEFT:
                    return left != null;
                case RIGHT:
                    return right != null;
                default:
                    return false;
            }
        }

        /**
         *
         * @param direction which direction to check
         * @return boolean value of if current node has branch in given direction based on
         *          boolean attributes
         */
        public boolean hasBranchInDirection(branchDirection direction){
            switch (direction) {
                case MIDDLE:
                    return hasMiddle;
                case LEFT:
                    return hasLeft;
                case RIGHT:
                    return hasRight;
                default:
                    return false;
            }
        }

        /**
         *
         * @return boolean value of if this node has any branches growing from it
         */
        public boolean hasBranch() {
            return hasMiddle || hasLeft || hasRight;
        }

    }

}