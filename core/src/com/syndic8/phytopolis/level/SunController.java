package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Texture;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Sun;
import com.syndic8.phytopolis.util.RandomController;
import com.syndic8.phytopolis.util.Tilemap;

public class SunController {

    private final float delayMin;
    private final float delayMax;
    private final float xGenerationMin;
    private final float xGenerationMax;
    private final float yGeneration;
    private float currentDelay;
    private float timer;
    private float xGeneration;
    private Texture sunCircle;
    private Texture sunRay;
    private Texture sunSwirl;

    public SunController(float dMin,
                         float dMax,
                         float xGenMin,
                         float xGenMax,
                         float yGen) {

        delayMin = dMin;
        delayMax = dMax;
        timer = 0;
        xGenerationMin = xGenMin;
        xGenerationMax = xGenMax;
        yGeneration = yGen;
        generateDelay();
    }

    private void generateDelay() {
        timer = 0;
        currentDelay = RandomController.rollFloat(delayMin, delayMax);
        xGeneration = RandomController.rollFloat(xGenerationMin,
                                                 xGenerationMax);
    }

    public void gatherAssets(AssetDirectory directory) {
        sunCircle = directory.getEntry("gameplay:sun_circle", Texture.class);
        sunSwirl = directory.getEntry("gameplay:sun_swirl", Texture.class);
        sunRay = directory.getEntry("gameplay:sun_ray", Texture.class);
    }

    public Sun spawnSuns(float delta, Tilemap tm) {
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
                           tm,
                           1);
        }
        return null;
    }

}
