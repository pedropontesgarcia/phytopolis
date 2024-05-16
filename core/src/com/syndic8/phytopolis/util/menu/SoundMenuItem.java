package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.SoundController;
import com.syndic8.phytopolis.SoundController.SoundOption;
import com.syndic8.phytopolis.util.SharedAssetContainer;

public class SoundMenuItem extends MenuItem {

    private final SoundOption option;
    private final TextButton headerLabel;
    private final Slider slider;

    public SoundMenuItem(String text,
                         int index,
                         Menu m,
                         MenuContainer ctr,
                         GameCanvas c,
                         SoundOption opn) {
        super(text, index, new ClickListener(), m, ctr, c);
        option = opn;
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
        float yPosHeader =
                wh / 2f + (m.getLength() - 1f) * m.getSeparation() * wh / 2f -
                        index * m.getSeparation() * wh -
                        headerLabel.getHeight() / 2f + wh * m.getYOffset();
        headerLabel.setPosition(xPosHeader, yPosHeader);

        slider = new Slider(0,
                            20,
                            0.1f,
                            false,
                            SharedAssetContainer.getInstance().getSliderSkin());
        slider.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event,
                                float x,
                                float y,
                                int pointer,
                                int button) {
                super.touchUp(event, x, y, pointer, button);
                SoundController.getInstance().playSound(0);
            }
        });
        float xPosSlider = ww / 2f + 0.015f * ww;
        float yPosSlider =
                wh / 2f + (m.getLength() - 1f) * m.getSeparation() * wh / 2f -
                        index * m.getSeparation() * wh -
                        slider.getHeight() / 2f + wh * m.getYOffset();
        slider.setPosition(xPosSlider, yPosSlider);
        slider.setWidth(m.getWidth());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundController.getInstance()
                        .updateOption(option,
                                      slider.getValue() / slider.getMaxValue());
            }
        });
        slider.setValue(SoundController.getInstance().getOptionValue(opn) *
                                slider.getMaxValue());
    }

    //    public void updateLabel() {
    //        getLabel().setText(SoundController.getInstance()
    //                                   .getOptionValueString(option));
    //    }

    public TextButton getHeaderLabel() {
        return headerLabel;
    }

    public Slider getSlider() {
        return slider;
    }

}
