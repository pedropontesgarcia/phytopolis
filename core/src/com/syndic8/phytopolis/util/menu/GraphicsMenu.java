package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.GameCanvas.GraphicsOption;

import java.util.ArrayList;
import java.util.List;

public class GraphicsMenu extends Menu {

    public GraphicsMenu(GameCanvas c, MenuContainer ctr, Menu back) {
        super(5, 0.075f, 0, 0f, 0.75f, Align.left, DEFAULT_WIDTH);
        addItem(new GraphicsMenuItem("RESOLUTION",
                                     0,
                                     this,
                                     ctr,
                                     c,
                                     GraphicsOption.RESOLUTION));
        addItem(new GraphicsMenuItem("WINDOWED MODE",
                                     1,
                                     this,
                                     ctr,
                                     c,
                                     GraphicsOption.WINDOWED));
        addItem(new GraphicsMenuItem("FRAME RATE",
                                     2,
                                     this,
                                     ctr,
                                     c,
                                     GraphicsOption.FPS));
        addItem(new BackButtonItem(back, this, ctr, c));
    }

    /**
     * Updates the labels so that they reflect the current settings.
     */
    public void updateGraphicssLabels() {
        for (MenuItem item : getItems()) {
            if (item instanceof GraphicsMenuItem) {
                ((GraphicsMenuItem) item).updateLabel();
            }
        }
    }

    @Override
    public List<TextButton> gatherLabels() {
        List<TextButton> labels = new ArrayList<>();
        for (MenuItem item : getItems()) {
            labels.add(item.getLabel());
            if (item instanceof GraphicsMenuItem) {
                labels.add(((GraphicsMenuItem) item).getHeaderLabel());
            }
        }
        return labels;
    }

}
