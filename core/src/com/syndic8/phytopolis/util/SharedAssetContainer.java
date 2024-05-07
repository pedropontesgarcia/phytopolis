package com.syndic8.phytopolis.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class containing assets that are shared across modes and that
 * are not loaded through the AssetDirectory due to technical limitations.
 */
public class SharedAssetContainer {

    private static SharedAssetContainer sharedAssetContainerInstance;
    public final Map<Float, BitmapFont> uiFontMap;
    private final float BASE_FONT_SCALE = 0.2f;
    private final FreeTypeFontGenerator uiFontGenerator;
    private final FreeTypeFontParameter uiFontParameter;
    private final Skin progressBarSkin;
    private final Skin sliderSkin;
    private final Map<String, Integer> soundMap;

    public SharedAssetContainer() {
        soundMap = new HashMap<>();
        uiFontMap = new HashMap<>();
        uiFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(
                "fonts/Krungthep.ttf"));
        FreeTypeFontGenerator.setMaxTextureSize(4096);
        uiFontParameter = new FreeTypeFontParameter();
        uiFontParameter.size = 256;
        uiFontParameter.color = Color.WHITE;
        uiFontParameter.shadowColor = new Color(0, 0.6f, 0.6f, 1);
        uiFontParameter.shadowOffsetX = 15;
        uiFontParameter.shadowOffsetY = 15;
        BitmapFont uiFont = uiFontGenerator.generateFont(uiFontParameter);
        uiFont.getRegion()
                .getTexture()
                .setFilter(Texture.TextureFilter.Linear,
                           Texture.TextureFilter.Linear);
        uiFont.getData().setScale(BASE_FONT_SCALE);
        uiFontMap.put(1f, uiFont);

        progressBarSkin = new Skin(Gdx.files.internal("ui/skins/fire.json"));
        sliderSkin = new Skin(Gdx.files.internal("ui/skins/slider.json"));
    }

    /**
     * Gets the singleton instance of this class, or creates one if there are
     * none.
     *
     * @return the singleton instance of this class.
     */
    public static SharedAssetContainer getInstance() {
        if (sharedAssetContainerInstance == null) {
            sharedAssetContainerInstance = new SharedAssetContainer();
        }
        return sharedAssetContainerInstance;
    }

    /**
     * Gets the UI font with the default scale.
     *
     * @return the UI font.
     */
    public BitmapFont getUIFont() {
        return uiFontMap.get(1f);
    }

    /**
     * Gets the UI font with a custom scale. Caches fonts so that if a
     * previous instance with the requested scale exists, it is returned
     * instead of creating a new one.
     *
     * @param scl the font scale, as a multiplier to the default scale.
     * @return the UI font with the requested scale.
     */
    public BitmapFont getUIFont(float scl) {
        if (uiFontMap.containsKey(scl)) {
            return uiFontMap.get(scl);
        } else {
            BitmapFont font = uiFontGenerator.generateFont(uiFontParameter);
            font.getData().setScale(BASE_FONT_SCALE * scl);
            uiFontMap.put(scl, font);
            return font;
        }
    }

    /**
     * Gets the skin for the fire progress bar.
     *
     * @return the skin for the fire progress bar.
     */
    public Skin getProgressBarSkin() {
        return progressBarSkin;
    }

    public Skin getSliderSkin() {
        return sliderSkin;
    }

    public void addSound(String name, int i) {
        soundMap.put(name, i);
    }

    public int getSound(String name) {
        return soundMap.get(name);
    }

}
