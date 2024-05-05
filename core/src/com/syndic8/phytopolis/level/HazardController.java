package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.*;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.PooledList;
import com.syndic8.phytopolis.util.Tilemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static com.syndic8.phytopolis.level.models.Model.ModelType.FIRE;

public class HazardController {

    /**
     * Random number generator for various hazard generation.
     */
    private final Random random = new Random();
    /**
     * Frame counter to switch from yellow and red warning.
     */
    private final int frameCounter = 0;
    /**
     * Reference to the PlantController.
     */
    private final PlantController plantController;
    /**
     * Reference to the PlantController.
     */
    private final ResourceController resourceController;
    /**
     * Texture for fire hazard.
     */
    protected FilmStrip fireTexture;
    /**
     * Texture for drone hazard.
     */
    protected Texture droneTexture;
    /**
     * Texture for bug hazard
     */
    protected FilmStrip bugTexture;
    /**
     * Texture for yellow warning indicator.
     */
    protected TextureRegion redWarningTexture;
    /**
     * Texture for red warning indicator.
     */
    protected TextureRegion redWarningFlashTexture;
    /**
     * Texture for warning indicator arrow.
     */
    protected TextureRegion redArrowDownTexture;
    /**
     * Texture for warning indicator arrow.
     */
    protected TextureRegion redArrowUpTexture;
    /**
     * List to track active hazards and their remaining time.
     */
    ArrayList<Hazard> hazards;
    ArrayList<Integer> fireNodes;
    ArrayList<Integer> bugNodes;
    PooledList<Hazard> addList;
    /**
     * The frequency at which fires are generated (probability = 1 / fireFrequency) every second.
     */
    private int fireFrequency;
    /**
     * ;
     * The frequency at which drones are generated (probability = 1 / droneFrequency) every second.
     */
    private int droneFrequency;
    /**
     * The frequency at which bugs are generated
     */
    private int bugFrequency;
    /**
     * The height of the game area.
     */
    private int height;
    /**
     * The width of the game area.
     */
    private int width;
    /**
     * The duration that a fire continues to burn in seconds. (fire duration)
     */
    private int burnTime;
    /**
     * The time until drone explodes after it spawns in seconds. (drone duration)
     */
    private int explodeTime;
    /**
     * The time it takes a bug to eat a leaf (bug duration)
     */
    private int eatTime;
    /**
     * Tilemap
     */
    private Tilemap tilemap;
    /**
     * Update timer for hazards
     */
    private long lastUpdateTime;
    private TextureRegion greenWarningTexture;
    private TextureRegion greenWarningFlashTexture;
    private TextureRegion greenArrowDownTexture;
    private TextureRegion greenArrowUpTexture;
    private PooledList<Float> powerlineHeights;
    private PooledList<Vector2> validFireLocs;
    private float FIRE_BUFFER = 5;
    private float fireProgress;

//    /**
//     * Initializes a HazardController with the given parameters.
//     *
//     * @param plantController The PlantController instance associated with this HazardController.
//     */
//    public HazardController(PlantController plantController, Tilemap tm) {
//        this(plantController, 8, 100000000, 6, 8, 6, 6, tm);
//    }

    /**
     * Initializes a HazardController with the given parameters.
     *
     * @param plantController The PlantController instance associated with this HazardController.
     * @param fireFrequency   The frequency at which fires are generated.
     * @param droneFrequency  The frequency at which drones are generated.
     * @param burnTime        The time duration for which fires burn.
     */
    public HazardController(PlantController plantController,
                            int fireFrequency,
                            int droneFrequency,
                            int bugFrequency,
                            int burnTime,
                            int explodeTime,
                            int eatTime,
                            PooledList<Float> powerlineHeights,
                            Tilemap tm) {
        this.fireFrequency = fireFrequency;
        this.droneFrequency = droneFrequency;
        this.bugFrequency = bugFrequency;
        this.plantController = plantController;
        this.resourceController = plantController.getResourceController();
        this.burnTime = burnTime;
        this.explodeTime = explodeTime;
        this.eatTime = eatTime;
        this.powerlineHeights = powerlineHeights;
        hazards = new ArrayList<>();
        fireNodes = new ArrayList<>();
        bugNodes = new ArrayList<>();
        addList = new PooledList<>();
        validFireLocs = new PooledList<>();
        height = plantController.getHeight();
        width = plantController.getWidth();
        tilemap = tm;
        fireProgress = 0;
    }

    public void reset(int fireFrequency,
                      int droneFrequency,
                      int bugFrequency,
                      int burnTime,
                      int explodeTime,
                      int eatTime,
                      Tilemap tm) {
        this.fireFrequency = fireFrequency;
        this.droneFrequency = droneFrequency;
        this.bugFrequency = bugFrequency;
        this.burnTime = burnTime;
        this.explodeTime = explodeTime;
        this.eatTime = eatTime;
        hazards = new ArrayList<>();
        fireNodes = new ArrayList<>();
        bugNodes = new ArrayList<>();
        addList = new PooledList<>();
        height = plantController.getHeight();
        width = plantController.getWidth();
        tilemap = tm;
    }

    public Hazard generateHazard(Model.ModelType type) {
        int hazardHeight = generateHazardHeight(type);
        if (hazardHeight == -1) return null;
        int hazardWidth = generateHazardWidth(hazardHeight, type);
        if (hazardWidth == -1) return null;
        return generateHazard(type, hazardWidth, hazardHeight);
    }

    /**
     * Generates a hazard at random node at a generated height if the time is right.
     *
     * @param type The type of hazard.
     * @return the generated hazard (null if none)
     */
    public Hazard generateHazard(Model.ModelType type, int x, int y) {
        switch (type) {
            case FIRE:
                if (isValidFireLocation(x, y)) {
                    return generateHazardAt(type, x, y);
                }
                break;
            case BUG:
                if (plantController.hasLeaf(x, y) &&
                        !plantController.hasHazard(x, y)) {
                    return generateHazardAt(type, x, y);
                }
                break;
        }
        return null;
    }

    public boolean isValidFireLocation(int x, int y) {
        return !plantController.hasHazard(x, y) &&
                ((plantController.inBounds(x - 1, y - 1) &&
                plantController.branchExists(x - 1,
                        y - 1,
                        PlantController.branchDirection.RIGHT)) ||
                (plantController.inBounds(x + 1, y - 1) &&
                        plantController.branchExists(x + 1,
                                y - 1,
                                PlantController.branchDirection.LEFT)) ||
                (plantController.inBounds(x, y - 1) &&
                        plantController.branchExists(x,
                                y - 1,
                                PlantController.branchDirection.MIDDLE)) ||
                (plantController.inBounds(x, y) &&
                        !plantController.nodeIsEmpty(x, y)));
    }

    /**
     * Generates a random height for a hazard based on the frequency.
     *
     * @return The height at which the hazard is generated, or -1 if no hazard is generated.
     */
    private int generateHazardHeight(Model.ModelType type) {
        int hazardHeight = -1;
        switch (type) {
            case FIRE:
                if (random.nextDouble() < 1.0 / fireFrequency)
                    hazardHeight = random.nextInt(height);
                break;
            case DRONE:
                if (random.nextDouble() < 1.0 / droneFrequency)
                    hazardHeight = random.nextInt(height);
                break;
            case BUG:
                if (random.nextDouble() < 1.0 / bugFrequency)
                    hazardHeight = random.nextInt(height);
                break;
            default:
                break;
        }
        hazardHeight = hazardHeight == -1 ? -1 : hazardHeight + 1;
        return Math.min(hazardHeight, height - 1);
    }

    /**
     * Generates a random width for a hazard based on the frequency.
     *
     * @return The width at which the hazard is generated, or -1 if no plant at that height.
     */
    private int generateHazardWidth(int hazardHeight, Model.ModelType type) {
        switch (type) {
            case FIRE:
                fireNodes.clear();
                // Check if the plant node exists and append to effected nodes
                for (int w = 0; w < width; w++) {
                    if (!plantController.nodeIsEmpty(w, hazardHeight)) {
                        fireNodes.add(w);
                    }
                }
                // Choose a node at random to destroy from effectedNodes
                if (!fireNodes.isEmpty()) {
                    int randomIndex = random.nextInt(fireNodes.size());
                    return fireNodes.get(randomIndex);
                }
                break;
            case BUG:
                bugNodes.clear();
                // Check if the plant node has a leaf and append to bugNodes
                for (int w = 0; w < width; w++) {
                    if (plantController.hasLeaf(w, hazardHeight)) {
                        bugNodes.add(w);
                    }
                }
                // Choose a node at random to destroy from bugNodes
                if (!bugNodes.isEmpty()) {
                    int randomIndex = random.nextInt(bugNodes.size());
                    return bugNodes.get(randomIndex);
                }
                break;
        }
        return -1;
    }

    /**
     * Generates a fire at a random node if the time is right.
     *
     * @return the generated fire (null if none)
     */
    public Fire generateFire() {
        Vector2 fireLoc = validFireLocs.get(random.nextInt(validFireLocs.size()));
        Hazard h = generateHazard(FIRE, (int)fireLoc.x, (int)fireLoc.y);
        if (h != null) {
            return (Fire) h;
        }
        return null;
    }

    public void findValidFireLocs() {
        validFireLocs.clear();
        for (float height : powerlineHeights) {
            if (plantController.getMaxLeafHeight() >= height) {
                int max = plantController.screenCoordToIndex(0, height + FIRE_BUFFER)[1];
                int min = plantController.screenCoordToIndex(0, height - FIRE_BUFFER)[1];
                for (int i = min; i <= max; i++) {
                    for (int width = 0; width < plantController.getWidth(); width++) {
                        if (isValidFireLocation(i, width)) {
                            validFireLocs.add(new Vector2(i, width));
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a drone at a random node if the time is right.
     *
     * @return the generated drone (null if none)
     */
    public Drone generateDrone() {
        Hazard h = generateHazard(Model.ModelType.DRONE);
        if (h != null) {
            return (Drone) h;
        }
        return null;
    }

    public Bug generateBug() {
        Hazard h = generateHazard(Model.ModelType.BUG);
        if (h != null) {
            return (Bug) h;
        }
        return null;
    }

    /**
     * Updates the hazards for the current state of the game. This method is responsible
     * for generating and managing both fire and drone hazards, including their effects
     * on plant nodes.
     *
     * @return list of new Fire objects to add
     */
    public PooledList<Hazard> updateHazards() {
        addList.clear();
        for (Hazard h : plantController.removeDeadLeafBugs()) {
            removeHazard(h);
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >=
                1000) { // Check if one second has passed
            lastUpdateTime = currentTime; // Reset the last update time
            if (fireProgress >= 100) {
                findValidFireLocs();
                addList.add(generateFire());
                fireProgress = 0;
            }
            addList.add(generateDrone());
            addList.add(generateBug());
            int i = 0;
            while (i < hazards.size()) {
                Hazard h = hazards.get(i);
                int hx = (int) h.getLocation().x;
                int hy = (int) h.getLocation().y;
                if (h instanceof Fire) {
                    Fire f = (Fire) h;
                    // check if branch is still there (floating fire bug)

                    if (plantController.nodeIsEmpty(hx, hy)) {
                        removeHazard(h);
                        plantController.removeHazardFromNodes(h);
                        continue; // Continue to next hazard after removing
                    }
                }
                // spread fire if the time is right, otherwise decrement timer
                //                        int time = f.getDuration();
                if (h.tick()) {
                    removeHazard(h);
                    plantController.removeHazardFromNodes(h);
                    if (h instanceof Fire) {
                        plantController.destroyAll(hx, hy);
                        spreadFire(h.getLocation());
                    }
                }
                i++;
            }
        }
        addList.removeAll(Collections.singleton(null));
        return addList;
    }

    public void removeHazard(Hazard h) {
        hazards.remove(h);
        h.markRemoved(true);
    }

    /**
     * Spreads the fire to adjacent nodes.
     */
    private void spreadFire(Vector2 node) {
        int x = (int) node.x;
        int y = (int) node.y;
        if (y + 1 <= height) {
            // check top left
            if (plantController.inBounds(x - 1, y + 1)) {
                if (!plantController.nodeIsEmpty(x - 1, y + 1) &&
                        !plantController.hasHazard(x - 1, y + 1)) {
                    addList.add(generateHazardAt(FIRE, x - 1, y + 1));
                }
            }
            // check top right
            if (plantController.inBounds(x + 1, y + 1)) {
                if (!plantController.nodeIsEmpty(x + 1, y + 1) &&
                        !plantController.hasHazard(x + 1, y + 1)) {
                    addList.add(generateHazardAt(FIRE, x + 1, y + 1));
                }
            }
            // check top middle
            if (plantController.inBounds(x, y + 1)) {
                if (!plantController.nodeIsEmpty(x, y + 1) &&
                        !plantController.hasHazard(x, y + 1)) {
                    addList.add(generateHazardAt(FIRE, x, y + 1));
                }
            }
        }

        if (y - 1 >= 0) {
            // check bottom left
            if (plantController.inBounds(x - 1, y - 1)) {
                if (plantController.branchExists(x - 1,
                                                 y - 1,
                                                 PlantController.branchDirection.RIGHT) &&
                        !plantController.hasHazard(x - 1, y + 1)) {
                    addList.add(generateHazardAt(FIRE, x - 1, y - 1));
                }
            }
            // check bottom right
            if (plantController.inBounds(x + 1, y - 1)) {
                if (plantController.branchExists(x + 1,
                                                 y - 1,
                                                 PlantController.branchDirection.LEFT)) {
                    addList.add(generateHazardAt(FIRE, x + 1, y - 1));
                }
            }
            // check bottom middle
            if (plantController.inBounds(x, y - 1)) {
                if (plantController.branchExists(x,
                                                 y - 1,
                                                 PlantController.branchDirection.MIDDLE)) {
                    addList.add(generateHazardAt(FIRE, x, y - 1));
                }
            }
        }
    }

    /**
     * Extinguish fire at the given mouse position.
     *
     * @param mousePos mouse position
     */
    public void extinguishFire(Vector2 mousePos) {
        if (!resourceController.canExtinguish()) return;
        for (Hazard h : hazards) {
            if (h.getType().equals(FIRE)) {
                Vector2 hazPos = plantController.indexToWorldCoord((int) h.getLocation().x,
                                                                   (int) h.getLocation().y);
                if (Math.abs(mousePos.x - hazPos.x) < .5 &&
                        Math.abs(mousePos.y - hazPos.y) < .5) {
                    hazards.remove(h);
                    h.markRemoved(true);
                    resourceController.decrementExtinguish();
                    break;
                }
            }
        }
    }

    private void moveBug(Vector2 node) {
        int x = (int) node.x;
        int y = (int) node.y;
        if (y + 1 <= height) {
            // check top left
            if (plantController.inBounds(x - 1, y + 1)) {
                if (!plantController.nodeIsEmpty(x - 1, y + 1)) {
                    addList.add(generateHazardAt(FIRE, x - 1, y + 1));
                }
            }
            // check top right
            if (plantController.inBounds(x + 1, y + 1)) {
                if (!plantController.nodeIsEmpty(x + 1, y + 1)) {
                    addList.add(generateHazardAt(FIRE, x + 1, y + 1));
                }
            }
            // check top middle
            if (plantController.inBounds(x, y + 1)) {
                if (!plantController.nodeIsEmpty(x, y + 1)) {
                    addList.add(generateHazardAt(FIRE, x, y + 1));
                }
            }
        }

        if (y - 1 >= 0) {
            // check bottom left
            if (plantController.inBounds(x - 1, y - 1)) {
                if (plantController.branchExists(x - 1,
                                                 y - 1,
                                                 PlantController.branchDirection.RIGHT)) {
                    addList.add(generateHazardAt(FIRE, x - 1, y - 1));
                }
            }
            // check bottom right
            if (plantController.inBounds(x + 1, y - 1)) {
                if (plantController.branchExists(x + 1,
                                                 y - 1,
                                                 PlantController.branchDirection.LEFT)) {
                    addList.add(generateHazardAt(FIRE, x + 1, y - 1));
                }
            }
            // check bottom middle
            if (plantController.inBounds(x, y - 1)) {
                if (plantController.branchExists(x,
                                                 y - 1,
                                                 PlantController.branchDirection.MIDDLE)) {
                    addList.add(generateHazardAt(FIRE, x, y - 1));
                }
            }
        }
    }

    /**
     * Generates a hazard at random node at a given x and y.
     *
     * @param type         The type of hazard.
     * @param hazardWidth  x coordinate
     * @param hazardHeight y coordinate
     * @return the generated hazard (null if none)
     */
    public Hazard generateHazardAt(Model.ModelType type,
                                   int hazardWidth,
                                   int hazardHeight) {
        switch (type) {
            case FIRE:
                Fire f = new Fire(plantController.indexToWorldCoord(hazardWidth,
                                                                    hazardHeight),
                                  new Vector2(hazardWidth, hazardHeight),
                                  burnTime,
                                  tilemap,
                                  0.5f);
                f.setFilmStrip(fireTexture);
                f.setAnimationSpeed(0.05f);
                plantController.setHazard(hazardWidth, hazardHeight, f);
                hazards.add(f);
                return f;
            case DRONE:
                Drone d = new Drone(plantController.indexToWorldCoord(
                        hazardWidth,
                        hazardHeight),
                                    new Vector2(hazardWidth, hazardHeight),
                                    explodeTime,
                                    tilemap,
                                    0.5f);
                d.setTexture(droneTexture);
                hazards.add(d);
                return d;
            case BUG:
                Bug b = new Bug(plantController.indexToWorldCoord(hazardWidth,
                                                                  hazardHeight),
                                new Vector2(hazardWidth, hazardHeight),
                                eatTime,
                                tilemap,
                                0.5f);
                //                System.out.println("new bug");
                b.setFilmStrip(bugTexture);
                b.setAnimationSpeed(0.05f);
                plantController.setHazard(hazardWidth, hazardHeight, b);
                hazards.add(b);
                return b;
            default:
                return null;
        }
    }

    /**
     * Returns true if there is a fire at the mouse location.
     *
     * @param mousePos mouse position in world coordinates.
     */
    public boolean hasFire(Vector2 mousePos) {
        for (Hazard h : hazards) {
            if (h.getType().equals(FIRE)) {
                Vector2 hazPos = plantController.indexToWorldCoord((int) h.getLocation().x,
                                                                   (int) h.getLocation().y);
                if (Math.abs(mousePos.x - hazPos.x) < .5 &&
                        Math.abs(mousePos.y - hazPos.y) < .5) return true;
            }
        }
        return false;
    }

    /**
     * Sets the texture of hazards.
     */
    public void gatherAssets(AssetDirectory directory) {
        this.fireTexture = new FilmStrip(directory.getEntry("hazards:fire",
                                                            Texture.class),
                                         1,
                                         16,
                                         16);
        this.droneTexture = directory.getEntry("hazards:drone", Texture.class);
        this.bugTexture = new FilmStrip(directory.getEntry("hazards:bug",
                                                           Texture.class),
                                        1,
                                        9,
                                        9);
        this.redWarningTexture = new TextureRegion(directory.getEntry(
                "hazards:red-warning",
                Texture.class));
        this.redWarningFlashTexture = new TextureRegion(directory.getEntry(
                "hazards:red-warning-flash",
                Texture.class));
        this.greenWarningTexture = new TextureRegion(directory.getEntry(
                "hazards:green-warning",
                Texture.class));
        this.greenWarningFlashTexture = new TextureRegion(directory.getEntry(
                "hazards:green-warning-flash",
                Texture.class));
        this.redArrowDownTexture = new TextureRegion(directory.getEntry(
                "hazards:arrow-down-red",
                Texture.class));
        this.redArrowUpTexture = new TextureRegion(directory.getEntry(
                "hazards:arrow-up-red",
                Texture.class));
        this.greenArrowDownTexture = new TextureRegion(directory.getEntry(
                "hazards:arrow-down-green",
                Texture.class));
        this.greenArrowUpTexture = new TextureRegion(directory.getEntry(
                "hazards:arrow-up-green",
                Texture.class));
    }

    /**
     * Draws a warning symbol if hazards are out of camera view.
     *
     * @param canvas       game canvas
     * @param cameraVector camera position
     */
    public void drawWarning(GameCanvas canvas, Vector2 cameraVector) {
        float w = tilemap.getWorldWidth();
        float hi = w * canvas.getHeight() / canvas.getWidth();
        for (Hazard h : hazards) {
            Vector2 hazardLoc = plantController.indexToWorldCoord((int) h.getLocation().x,
                                                                  (int) h.getLocation().y);
            if (Math.abs(hazardLoc.y - cameraVector.y) > hi / 2f) {
                // begin magic numbers
                float warningScale = 1f;
                float arrowScale = 0.3f; // works
                float warningVSep = 0.85f; // source:
                float arrowVSep = 0.2f; // trust me bro
                // end magic numbers
                float warningX = hazardLoc.x - warningScale / 2f;
                float warningY = (hazardLoc.y < cameraVector.y ?
                        warningVSep :
                        hi - warningVSep) - warningScale / 2f + cameraVector.y -
                        hi / 2f;
                float arrowX = hazardLoc.x - arrowScale / 2f;
                float arrowY = (hazardLoc.y < cameraVector.y ?
                        arrowVSep :
                        hi - arrowVSep) - arrowScale / 2f + cameraVector.y -
                        hi / 2f;
                TextureRegion arrowTex = hazardLoc.y < cameraVector.y ?
                        (h.getType() == FIRE ?
                                redArrowDownTexture :
                                greenArrowDownTexture) :
                        (h.getType() == FIRE ?
                                redArrowUpTexture :
                                greenArrowUpTexture);

                // Choose texture based on current time
                long currentTime = System.currentTimeMillis();
                System.out.println(h.getTimer());
                int interval = (int) (500f * (float) h.getTimer() /
                        (float) h.getMaxTimer());
                TextureRegion warningTex = (currentTime / interval) % 2 == 0 ?
                        (h.getType() == FIRE ?
                                redWarningTexture :
                                greenWarningTexture) :
                        (h.getType() == FIRE ?
                                redWarningFlashTexture :
                                greenWarningFlashTexture);

                canvas.draw(warningTex,
                            Color.WHITE,
                            warningX,
                            warningY,
                            warningScale,
                            warningScale);
                canvas.draw(arrowTex,
                            Color.WHITE,
                            arrowX,
                            arrowY,
                            arrowScale,
                            arrowScale);
            }
        }
    }

    public void update(float dt) {
        fireProgress += dt * 10 * powerlinesTouching();
    }

    public int powerlinesTouching() {
        int count = 0;
        for (float f : powerlineHeights) {
            if (plantController.getMaxLeafHeight() >= f) {
                count++;
            }
        }
        return count;
    }

    public void reset() {
        hazards.clear();
        fireNodes.clear();
        bugNodes.clear();
        addList.clear();
    }

}
