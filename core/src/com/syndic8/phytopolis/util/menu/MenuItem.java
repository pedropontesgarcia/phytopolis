package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.syndic8.phytopolis.GameCanvas;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {

    private final TextButton label;
    private final Menu submenu;

    public MenuItem(String text,
                    float sep,
                    int index,
                    int len,
                    ClickListener l,
                    GameCanvas c) {
        this(text, sep, index, len, l, null, c);
    }

    public MenuItem(String text,
                    float sep,
                    int index,
                    int len,
                    ClickListener l,
                    Menu sm,
                    GameCanvas c) {
        submenu = sm;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(
                "fonts/Krungthep.ttf"));
        FreeTypeFontGenerator.setMaxTextureSize(4096);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 256;
        parameter.color = Color.WHITE;
        parameter.shadowColor = new Color(0, 0.6f, 0.6f, 1);
        parameter.shadowOffsetX = 15;
        parameter.shadowOffsetY = 15;
        BitmapFont font = generator.generateFont(parameter);
        font.getRegion()
                .getTexture()
                .setFilter(Texture.TextureFilter.Linear,
                           Texture.TextureFilter.Linear);
        font.getData().setScale(0.2f);
        generator.dispose();
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.overFontColor = new Color(0.7f, 0.7f, 0.7f, 1);

        label = new TextButton(text, buttonStyle);
        float xPos = c.getTextViewport().getWorldWidth() / 2f -
                label.getWidth() / 2f;
        float yPos = c.getTextViewport().getWorldHeight() / 2f +
                (len - 1f) * sep / 2f - index * sep - label.getHeight() / 2f;
        label.setPosition(xPos, yPos);
        label.addListener(l);
    }

    public List<TextButton> gatherLabels() {
        List<TextButton> labels = new ArrayList<>();
        labels.add(label);
        if (submenu != null) labels.addAll(submenu.gatherLabels());
        return labels;
    }

}
