package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.InputController.Binding;
import com.syndic8.phytopolis.SoundController;
import com.syndic8.phytopolis.util.SharedAssetContainer;

public class ControlsMenuItem extends MenuItem {

    private final Binding binding;
    private final TextButton headerLabel;

    public ControlsMenuItem(String text,
                            int index,
                            Menu m,
                            MenuContainer ctr,
                            GameCanvas c,
                            Binding b) {
        super(text, index, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                InputController.getInstance().updateBinding(b);
            }
        }, m, ctr, c);
        binding = b;
        BitmapFont font = SharedAssetContainer.getInstance()
                .getUIFont(m.getFontScale());
        TextButton.TextButtonStyle labelStyle = new TextButton.TextButtonStyle();
        labelStyle.font = font;

        headerLabel = new TextButton(text, labelStyle);
        headerLabel.setSize(400, headerLabel.getMaxHeight());
        headerLabel.getLabel().setAlignment(Align.right);
        float ww = c.getTextViewport().getWorldWidth();
        float wh = c.getTextViewport().getWorldHeight();
        float xPosHeader = ww / 2f - headerLabel.getWidth() - 0.015f * ww;
        float xPosButton = ww / 2f + 0.015f * ww;
        float yPos =
                wh / 2f + (m.getLength() - 1f) * m.getSeparation() * wh / 2f -
                        index * m.getSeparation() * wh -
                        headerLabel.getHeight() / 2f + wh * m.getYOffset();
        headerLabel.setPosition(xPosHeader, yPos);
        getLabel().setPosition(xPosButton, yPos);
        TextButton.TextButtonStyle style = getLabel().getStyle();
        style.fontColor = new Color(0.6f, 0.6f, 0.6f, 1);
        style.overFontColor = Color.WHITE;
        getLabel().setStyle(style);
    }

    public void updateLabel() {
        getLabel().setText(
                "[" + InputController.getInstance().getBindingString(binding) +
                        "]");
    }

    public TextButton getHeaderLabel() {
        return headerLabel;
    }

}
