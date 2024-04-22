package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.WorldController;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Drone;
import com.syndic8.phytopolis.level.models.Fire;
import com.syndic8.phytopolis.level.models.Hazard;
import com.syndic8.phytopolis.level.models.Model;
import com.syndic8.phytopolis.util.Tilemap;

import java.util.ArrayList;
import java.util.Random;

public class HazardController {

    /**
     * The frequency at which fires are generated (probability = 1 / fireFrequency) every second.
     */
    private final int fireFrequency;
    /**
     * The frequency at which drones are generated (probability = 1 / droneFrequency) every second.
     */
    private final int droneFrequency;
    /**
     * Random number generator for various hazard generation.
     */
    private final Random random = new Random();
    /**
     * Reference to the PlantController.
     */
    private final PlantController plantController;
    /**
     * Reference to the PlantController.
     */
    private final ResourceController resourceController;
    /**
     * The height of the game area.
     */
    private final int height;
    /**
     * The width of the game area.
     */
    private final int width;
    /**
     * The duration that a fire continues to burn in seconds.
     */
    private final int burnTime;
    /**
     * The time until drone explodes after it spawns in seconds.
     */
    private final int explodeTime;
    /**
     * Update timer for hazards
     */
    private long lastUpdateTime;
    /**
     * Tilemap
     */
    private final Tilemap tilemap;
    /**
     * Texture for fire hazard.
     */
    protected Texture fireTexture;
    /**
     * Texture for drone hazard.
     */
    protected Texture droneTexture;
    /**
     * Texture for yellow warning indicator.
     */
    protected TextureRegion redWarningTexture;
    /**
     * Texture for red warning indicator.
     */
    protected TextureRegion yellowWarningTexture;
    /**
     * Frame counter to switch from yellow and red warning.
     */
    private int frameCounter = 0;
    /**
     * Texture for warning indicator arrow.
     */
    protected TextureRegion arrowDownTexture;
    /**
     * Texture for warning indicator arrow.
     */
    protected TextureRegion arrowUpTexture;
    /**
     * List to track active hazards and their remaining time.
     */
    ArrayList<Hazard> hazards;

    /**
     * Initializes a HazardController with the given parameters.
     *
     * @param plantController The PlantController instance associated with this HazardController.
     */
    public HazardController(PlantController plantController, Tilemap tm) {
        this(plantController, 300, 300, 100, 100, tm);
    }

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
                            int burnTime,
                            int explodeTime,
                            Tilemap tm) {
        this.fireFrequency = fireFrequency;
        this.droneFrequency = droneFrequency;
        this.plantController = plantController;
        this.resourceController = plantController.getResourceController();
        this.burnTime = burnTime;
        this.explodeTime = explodeTime;
        hazards = new ArrayList<>();
        height = plantController.getHeight();
        width = plantController.getWidth();
        tilemap = tm;
    }

    /**
     * Generates a hazard at random node at a generated height if the time is right.
     *
     * @param type The type of hazard.
     */
    public void generateHazard(Model.ModelType type) {
        int hazardHeight = generateHazardHeight(type);
        if (hazardHeight == -1) return;
        int hazardWidth = generateHazardWidth(hazardHeight);
        if (hazardWidth == -1) return;
        generateHazard(type, hazardWidth, hazardHeight);
    }

    /**
     * Generates a hazard at random node at a given x and y.
     *
     * @param type         The type of hazard.
     * @param hazardWidth  x coordinate
     * @param hazardHeight y coordinate
     */
    public void generateHazard(Model.ModelType type,
                               int hazardWidth,
                               int hazardHeight) {
        switch (type) {
            case FIRE:
                Fire f = new Fire(plantController.indexToWorldCoord(hazardWidth, hazardHeight),
                        new Vector2(hazardWidth, hazardHeight),
                                  burnTime,
                                  tilemap,
                                  0.5f);
                f.setTexture(fireTexture);
                hazards.add(f);
                break;
            case DRONE:
                Drone d = new Drone(plantController.indexToWorldCoord(hazardWidth, hazardHeight),
                        new Vector2(hazardWidth, hazardHeight),
                                    explodeTime,
                                    tilemap,
                                    0.5f);
                d.setTexture(droneTexture);
                hazards.add(d);
                break;
            default:
        }
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
            default:
                break;
        }
        return hazardHeight;
    }

    /**
     * Generates a random width for a hazard based on the frequency.
     *
     * @return The width at which the hazard is generated, or -1 if no plant at that height.
     */
    private int generateHazardWidth(int hazardHeight) {
        ArrayList<Integer> effectedNodes = new ArrayList<>();
        // Check if the plant node exists and append to effected nodes
        for (int w = 0; w < width; w++) {
            if (!plantController.nodeIsEmpty(w, hazardHeight)) {
                effectedNodes.add(w);
            }
        }
        // Choose a node at random to destroy from effectedNodes
        if (!effectedNodes.isEmpty()) {
            int randomIndex = random.nextInt(effectedNodes.size());
            return effectedNodes.get(randomIndex);
        }
        return -1;
    }

    /**
     * Updates the hazards for the current state of the game. This method is responsible
     * for generating and managing both fire and drone hazards, including their effects
     * on plant nodes.
     */
    public void updateHazards() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 1000) { // Check if one second has passed
            lastUpdateTime = currentTime; // Reset the last update time
            generateHazard(Model.ModelType.FIRE);
            generateHazard(Model.ModelType.DRONE);
            int i = 0;
            while (i < hazards.size()) {
                Hazard h = hazards.get(i);
                switch (h.getType()) {
                    case FIRE:
                        Fire f = (Fire) h;
                        // check if branch is still there (floating fire bug)
                        int fx = (int) f.getLocation().x;
                        int fy = (int) f.getLocation().y;
                        if (plantController.nodeIsEmpty(fx, fy)) {
                            hazards.remove(f);
                            continue; // Continue to next hazard after removing
                        }
                        // spread fire if the time is right, otherwise decrement timer
                        int time = f.getDuration();
                        if (time == 1) {
                            hazards.remove(f);
                            f.markRemoved(true);
                            plantController.destroyAll(fx, fy);
                            spreadFire(f.getLocation());
                        } else {
                            f.setDuration(time - 1);
                        }
                        break;
                    case DRONE:
                        Drone d = (Drone) h;
                        int time2 = d.getTimer();
                        if (time2 == 1) {
                            hazards.remove(d);
                            d.markRemoved(true);
                            plantController.destroyAll((int) d.getLocation().x,
                                    (int) d.getLocation().y);
                        } else {
                            d.setTimer(time2 - 1);
                        }
                        break;
                    default:
                        return; // Shouldn't reach here, but just in case
                }
                i++;
            }
        }
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
                if (!plantController.nodeIsEmpty(x - 1, y + 1)) {
                    generateHazard(Model.ModelType.FIRE, x - 1, y + 1);
                }
            }
            // check top right
            if (plantController.inBounds(x + 1, y + 1)) {
                if (!plantController.nodeIsEmpty(x + 1, y + 1)) {
                    generateHazard(Model.ModelType.FIRE, x + 1, y + 1);
                }
            }
            // check top middle
            if (plantController.inBounds(x, y + 1)) {
                if (!plantController.nodeIsEmpty(x, y + 1)) {
                    generateHazard(Model.ModelType.FIRE, x, y + 1);
                }
            }
        }

        if (y - 1 >= 0) {
            // check bottom left
            if (plantController.inBounds(x - 1, y - 1)) {
                if (plantController.branchExists(x - 1,
                                                 y - 1,
                                                 PlantController.branchDirection.RIGHT)) {
                    generateHazard(Model.ModelType.FIRE, x - 1, y - 1);
                }
            }
            // check bottom right
            if (plantController.inBounds(x + 1, y - 1)) {
                if (plantController.branchExists(x + 1,
                                                 y - 1,
                                                 PlantController.branchDirection.LEFT)) {
                    generateHazard(Model.ModelType.FIRE, x + 1, y - 1);
                }
            }
            // check bottom middle
            if (plantController.inBounds(x, y - 1)) {
                if (plantController.branchExists(x,
                                                 y - 1,
                                                 PlantController.branchDirection.MIDDLE)) {
                    generateHazard(Model.ModelType.FIRE, x, y - 1);
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
            if (h.getType().equals(Model.ModelType.FIRE)) {
                Vector2 hazPos = plantController.indexToWorldCoord((int) h.getLocation().x, (int) h.getLocation().y);
                if (Math.abs(mousePos.x - hazPos.x) < .5 && Math.abs(mousePos.y - hazPos.y) < .5) {
                    hazards.remove(h);
                    h.markRemoved(true);
                    resourceController.decrementExtinguish();
                    break;
                }
            }
        }
    }

    /**
     * Returns true if there is a fire at the mouse location.
     *
     * @param mousePos mouse position in world coordinates.
     */
    public boolean hasFire(Vector2 mousePos) {
        for (Hazard h : hazards) {
            if (h.getType().equals(Model.ModelType.FIRE)) {
                Vector2 hazPos = plantController.indexToWorldCoord((int) h.getLocation().x, (int) h.getLocation().y);
                if (Math.abs(mousePos.x - hazPos.x) < .5 && Math.abs(mousePos.y - hazPos.y) < .5) return true;
            }
        }
        return false;
    }

    /**
     * Sets the texture of hazards.
     */
    public void gatherAssets(AssetDirectory directory) {
        this.fireTexture = directory.getEntry("hazards:fire", Texture.class);
        this.droneTexture = directory.getEntry("hazards:drone", Texture.class);
        this.redWarningTexture = new TextureRegion(directory.getEntry("hazards:red-warning", Texture.class));
        this.yellowWarningTexture = new TextureRegion(directory.getEntry("hazards:yellow-warning", Texture.class));
        this.arrowDownTexture = new TextureRegion(directory.getEntry("hazards:arrow-down", Texture.class));
        this.arrowUpTexture = new TextureRegion(directory.getEntry("hazards:arrow-up", Texture.class));
    }

    /**
     * Draws a warning symbol if hazards are out of camera view.
     *
     * @param canvas game canvas
     * @param cameraVector camera position
     */
    public void drawWarning(GameCanvas canvas, Vector2 cameraVector) {
        int w = canvas.getWidth();
        int hi = canvas.getHeight();

        for (Hazard h : hazards) {
            Vector2 hazardLoc = plantController.indexToWorldCoord((int) h.getLocation().x, (int) h.getLocation().y);
            if (Math.abs(hazardLoc.y - cameraVector.y) > 4.5) {
                float warningY = hazardLoc.y < cameraVector.y ? 0.05f : 0.86f;
                float arrowY = hazardLoc.y < cameraVector.y ? 0.02f : 0.95f;
                TextureRegion arrowTex = hazardLoc.y < cameraVector.y ? arrowDownTexture : arrowUpTexture;

                // Choose texture based on current time
                long currentTime = System.currentTimeMillis();
                long interval = 500; // switch between red and yellow every .5 second
                TextureRegion warningTex = (currentTime / interval) % 2 == 0 ? redWarningTexture : yellowWarningTexture;

                float warningScale = 0.05f;
                float arrowScale = 0.02f;
                float warningX = hazardLoc.x - .4f;
                float arrowX = hazardLoc.x - .15f;

                canvas.drawHud(warningTex, warningX, hi * warningY, w * warningScale, w * warningScale);
                canvas.drawHud(arrowTex, arrowX, hi * arrowY, w * arrowScale, w * arrowScale);
            }
        }
    }

    /**
     * Draw each hazard.
     */
    public void draw(GameCanvas canvas) {
        for (Hazard h : hazards) {
            h.draw(canvas);
//            switch (h.getType()) {
//                case FIRE:
//                    Fire f = (Fire) h;
//                    f.draw(canvas);
//                    break;
//                case DRONE:
//                    Drone d = (Drone) h;
//                    d.draw(canvas);
//                    break;
//                default:
//                    return;
//            }
            // draw warning indicator

        }
    }

}
