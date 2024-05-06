package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.SoundController.SoundOption;

import java.util.ArrayList;
import java.util.List;

public class SoundMenu extends Menu {

    public SoundMenu(GameCanvas c, MenuContainer ctr, Menu back) {
        super(5, 0.075f, 0, 0f, 0.75f, Align.left, DEFAULT_WIDTH);
        addItem(new SoundMenuItem("MUSIC VOLUME",
                                  1,
                                  this,
                                  ctr,
                                  c,
                                  SoundOption.MUSIC_VOLUME));
        addItem(new SoundMenuItem("FX VOLUME",
                                  2,
                                  this,
                                  ctr,
                                  c,
                                  SoundOption.FX_VOLUME));
        addItem(new BackButtonItem(back, this, ctr, c));
    }

    //    /**
    //     * Updates the labels so that they reflect the current settings.
    //     */
    //    public void updateSoundLabels() {
    //        for (MenuItem item : getItems()) {
    //            if (item instanceof SoundMenuItem) {
    //                ((SoundMenuItem) item).updateLabel();
    //            }
    //        }
    //    }

    @Override
    public List<TextButton> gatherLabels() {
        List<TextButton> labels = new ArrayList<>();
        for (MenuItem item : getItems()) {
            if (item instanceof SoundMenuItem) {
                labels.add(((SoundMenuItem) item).getHeaderLabel());
            } else {
                labels.add(item.getLabel());
            }
        }
        return labels;
    }

    public List<Slider> gatherSliders() {
        List<Slider> sliders = new ArrayList<>();
        for (MenuItem item : getItems()) {
            if (item instanceof SoundMenuItem) {
                sliders.add(((SoundMenuItem) item).getSlider());
            }
        }
        return sliders;
    }

}
