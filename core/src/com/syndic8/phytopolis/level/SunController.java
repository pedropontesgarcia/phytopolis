package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Texture;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Sun;
import com.syndic8.phytopolis.util.RandomController;
import com.syndic8.phytopolis.util.Tilemap;

import java.util.List;

public class SunController {

    private final float delayMin;
    private final float delayMax;
    private float yGeneration;
    private List<Float> plantXPositions;
    private float currentDelay;
    private float timer;
    private float xGeneration;
    private Texture sunCircle;
    private Texture sunRay;
    private Texture sunSwirl;

    public SunController(float dMin,
                         float dMax,
                         float yGen,
                         List<Float> plantXs) {
        delayMin = dMin;
        delayMax = dMax;
        timer = 0;
        yGeneration = yGen;
        plantXPositions = plantXs;
        generateDelay();
    }

    private void generateDelay() {
        timer = 0;
        currentDelay = RandomController.rollFloat(delayMin, delayMax);
        xGeneration = plantXPositions.get(RandomController.rollInt(0,
                                                                   plantXPositions.size() -
                                                                           1));
    }

    public void reset(float yGen, List<Float> plantXs) {
        timer = 0;
        yGeneration = yGen;
        plantXPositions = plantXs;
        generateDelay();
    }

    public void gatherAssets(AssetDirectory directory) {
        sunCircle = directory.getEntry("gameplay:sun_circle", Texture.class);
        sunSwirl = directory.getEntry("gameplay:sun_swirl", Texture.class);
        sunRay = directory.getEntry("gameplay:sun_ray", Texture.class);
    }

    public Sun spawnSuns(float delta, Tilemap tm) {
        if (tm.getLevelNumber() != 1) {
            timer += delta;
            if (timer >= currentDelay) {
                generateDelay();
                return new Sun(xGeneration,
                               yGeneration,
                               tm.getTileWidth() * 0.5f,
                               tm.getTileHeight() * 0.5f,
                               sunCircle,
                               sunRay,
                               sunSwirl,
                               tm.getTilemapParams(),
                               1);
            }
        }
        return null;
    }

}
