package com.syndic8.phytopolis.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.HashMap;
import java.util.Map;

public class SharedAssetContainer {

    private static SharedAssetContainer sharedAssetContainerInstance;
    public final Map<Float, BitmapFont> uiFontMap;
    private final float BASE_FONT_SCALE = 0.2f;
    private final FreeTypeFontGenerator uiFontGenerator;
    private final FreeTypeFontParameter uiFontParameter;

    public SharedAssetContainer() {
        uiFontMap = new HashMap<Float, BitmapFont>();
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
    }

    public static SharedAssetContainer getInstance() {
        if (sharedAssetContainerInstance == null) {
            sharedAssetContainerInstance = new SharedAssetContainer();
        }
        return sharedAssetContainerInstance;
    }

    public BitmapFont getUIFont() {
        return uiFontMap.get(1f);
    }

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

}
