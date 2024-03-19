package com.syndic8.phytopolis.level;

import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.level.models.Drone;
import com.syndic8.phytopolis.level.models.Fire;
import com.syndic8.phytopolis.level.models.Hazard;
import com.syndic8.phytopolis.level.models.Model;

import java.util.*;

import static com.syndic8.phytopolis.level.models.Model.ModelType.DRONE;

public class HazardController {

    /**
     * The frequency at which fires are generated (probability = 1 / fireFrequency).
     */
    private final int fireFrequency;
    /**
     * The frequency at which drones are generated (probability = 1 / droneFrequency).
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
     * The height of the game area.
     */
    private final int height;
    /**
     * The width of the game area.
     */
    private final int width;
    /**
     * The duration that a fire continues to burn.
     */
    private final int burnTime;
    /**
     * List to track active hazards and their remaining time.
     */
    ArrayList<Hazard> hazards;

    /**
     * Initializes a HazardController with the given parameters.
     *
     * @param plantController The PlantController instance associated with this HazardController.
     */
    public HazardController(PlantController plantController) {
        this.fireFrequency = 3; // 1/3 chance to spawn fire whenever method is called
        this.droneFrequency = 3;
        this.plantController = plantController;
        this.burnTime = 5;
        hazards = new ArrayList<>();
        height = plantController.getHeight();
        width = plantController.getWidth();
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
                            int burnTime) {
        this.fireFrequency = fireFrequency;
        this.droneFrequency = droneFrequency;
        this.plantController = plantController;
        this.burnTime = burnTime;
        hazards = new ArrayList<>();
        height = plantController.getHeight();
        width = plantController.getWidth();
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
        Hazard hazard;
        switch (type) {
            case FIRE:
                hazard = new Fire(new Vector2(hazardWidth, hazardHeight), burnTime);
                break;
            case DRONE:
                hazard = new Drone(new Vector2(hazardWidth, hazardHeight));
                break;
            default:
                return;
        }
        hazards.add(hazard);
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



}
