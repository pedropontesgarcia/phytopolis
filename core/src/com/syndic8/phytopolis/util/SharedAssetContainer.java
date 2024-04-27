package com.syndic8.phytopolis.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class SharedAssetContainer {

    private static SharedAssetContainer sharedAssetContainerInstance;
    public final BitmapFont uiFont;

    public SharedAssetContainer() {
        FreeTypeFontGenerator uiFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(
                "fonts/Krungthep.ttf"));
        FreeTypeFontGenerator.setMaxTextureSize(4096);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 256;
        parameter.color = Color.WHITE;
        parameter.shadowColor = new Color(0, 0.6f, 0.6f, 1);
        parameter.shadowOffsetX = 15;
        parameter.shadowOffsetY = 15;
        uiFont = uiFontGenerator.generateFont(parameter);
        uiFont.getRegion()
                .getTexture()
                .setFilter(Texture.TextureFilter.Linear,
                           Texture.TextureFilter.Linear);
        uiFont.getData().setScale(0.2f);
        uiFontGenerator.dispose();
    }

    public static SharedAssetContainer getInstance() {
        if (sharedAssetContainerInstance == null) {
            sharedAssetContainerInstance = new SharedAssetContainer();
        }
        return sharedAssetContainerInstance;
    }

}
