package com.syndic8.phytopolis.level;

import com.badlogic.gdx.graphics.Texture;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.models.Sun;
import com.syndic8.phytopolis.util.FilmStrip;
import com.syndic8.phytopolis.util.Tilemap;

public class SunController {

    private final float delayMin;
    private final float delayMax;
    private final float xGenerationMin;
    private final float xGenerationMax;
    private final float yGeneration;
    private FilmStrip sunFilmstrip;
    private float currentDelay;
    private float timer;
    private float xGeneration;

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
        currentDelay = randomBetween(delayMin, delayMax);
        xGeneration = randomBetween(xGenerationMin, xGenerationMax);
    }

    private float randomBetween(float lower, float upper) {
        return lower + (float) Math.random() * (upper - lower);
    }

    public void gatherAssets(AssetDirectory directory) {
        Texture sunTexture = directory.getEntry("gameplay:sun_resource",
                                                Texture.class);
        sunFilmstrip = new FilmStrip(sunTexture, 1, 1);
    }

    public Sun spawnSuns(float delta, Tilemap tm) {
        timer += delta;
        if (timer >= currentDelay) {
            generateDelay();
            Sun s = new Sun(xGeneration,
                            yGeneration,
                            tm.getTileWidth() * 0.5f,
                            tm.getTileHeight() * 0.5f,
                            sunFilmstrip,
                            tm,
                            1);
            return s;
        }
        return null;
    }

}
