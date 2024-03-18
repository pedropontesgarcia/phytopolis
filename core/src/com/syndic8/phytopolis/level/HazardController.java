package com.syndic8.phytopolis.level;

import com.badlogic.gdx.math.Vector2;

import java.util.*;

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
     * HashMap to track active fires and their remaining burn time.
     */
    Map<Vector2, Integer> fires;
    /**
     * Cached ArrayList to keep track of drone nodes
     */
    ArrayList<Integer> droneNodes = new ArrayList<>();

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
        fires = new HashMap<>();
        height = plantController.getHeight();
        width = plantController.getWidth();
    }

    /**
     * Generates a random height for a fire based on the fire frequency.
     *
     * @return The height at which the fire is generated, or -1 if no fire is generated.
     */
    private int generateFireHeight() {
        if (random.nextDouble() < 1.0 / fireFrequency) {
            return random.nextInt(height);
        } else {
            return -1;
        }
    }

    /**
     * Generates a random height for a drone based on the drone frequency.
     *
     * @return The height at which the drone is generated, or -1 if no drone is generated.
     */
    private int generateDroneHeight() {
        if (random.nextDouble() < 1.0 / droneFrequency) {
            return random.nextInt(height);
        } else {
            return -1;
        }
    }

    /**
     * Generates a fire at random node at a generated height if the time is right.
     */
    private void generateFire() {
        int fireHeight = generateFireHeight();
        ArrayList<Integer> effectedNodes = new ArrayList<>();
        if (fireHeight != -1) {
            // Check if the plant node exists and append to effected nodes
            /*for (int w = 0; w < width; w++) {
                if (plantController.branchExists(w, fireHeight)) {
                    effectedNodes.add(w);
                }
            }*/
            // Choose a node at random to destroy from effectedNodes
            if (!effectedNodes.isEmpty()) {
                int randomIndex = random.nextInt(effectedNodes.size());
                int fireWidth = effectedNodes.get(randomIndex);
                fires.put(new Vector2(fireWidth, fireHeight), burnTime);
            }
        }
    }

    /**
     * Spreads the fire to adjacent nodes if the burn timer has finished.
     */
    private void spreadFire(Vector2 node) {

    }

    /**
     * Decrements the burn timers
     */
    public void updateBurnTimer() {
        Set<Vector2> keys = fires.keySet();
        for (Vector2 k : keys) {
            int timer = fires.get(k);
            if (timer - 1 <= 0) {
                spreadFire(k);
                fires.remove(k);
            } else {
                fires.put(k, timer - 1);
            }
        }
    }

    /**
     * Generates a drone at random node at a generated height if the time is right.
     */
    private void generateDrone() {
        int droneHeight = generateDroneHeight();
        if (droneHeight != -1) {
            System.out.println(droneHeight);
        }
        if (droneHeight != -1) {
            // Check if the plant node exists and append to effected nodes
            /*for (int w = 0; w < width; w++) {
                if (plantController.existsBranch(w, droneHeight)) {
                    System.out.println("width " + w);
                    droneNodes.add(w);
                }
            }*/
            // Choose a node at random to destroy from droneNodes
            if (!droneNodes.isEmpty()) {
                int randomIndex = random.nextInt(droneNodes.size());
                int nodeToDestroy = droneNodes.get(randomIndex);
                //plantController.destroyNode(nodeToDestroy, droneHeight);
            }
            droneNodes.clear();
        }
    }

    /**
     * Updates the hazards for the current state of the game. This method is responsible
     * for generating and managing both fire and drone hazards, including their effects
     * on plant nodes.
     */
    public void updateHazards() {
        //        generateFire();
        generateDrone();
        //        updateBurnTimer();
    }

}
