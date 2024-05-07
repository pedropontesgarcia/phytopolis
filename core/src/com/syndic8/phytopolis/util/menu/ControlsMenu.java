package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.SoundController;
import com.syndic8.phytopolis.util.SharedAssetContainer;

import java.util.ArrayList;
import java.util.List;

public class ControlsMenu extends Menu {

    private final TextButton topLabel;

    public ControlsMenu(GameCanvas c, MenuContainer ctr, Menu back) {
        super(10, 0.075f, 0, -0.015f, 0.75f, Align.left, DEFAULT_WIDTH);
        topLabel = makeTopLabel(c);
        addItem(new ControlsMenuItem("JUMP",
                                     0,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.JUMP_KEY));
        addItem(new ControlsMenuItem("LEFT",
                                     1,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.LEFT_KEY));
        addItem(new ControlsMenuItem("RIGHT",
                                     2,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.RIGHT_KEY));
        addItem(new ControlsMenuItem("DROP",
                                     3,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.DROP_KEY));
        addItem(new ControlsMenuItem("GROW BRANCH",
                                     4,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.GROW_BRANCH_BUTTON));
        addItem(new ControlsMenuItem("GROW BRANCH MOD",
                                     5,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.GROW_BRANCH_MOD_KEY));
        addItem(new ControlsMenuItem("GROW LEAF",
                                     6,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.GROW_LEAF_BUTTON));
        addItem(new ControlsMenuItem("GROW LEAF MOD",
                                     7,
                                     this,
                                     ctr,
                                     c,
                                     InputController.Binding.GROW_LEAF_MOD_KEY));
        addItem(new MenuItem("RESET", 9, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                InputController.getInstance().resetBindings();
            }
        }, this, ctr, c, Align.center, this.getFontScale(), 0.15f, 0, 200));
        addItem(new BackButtonItem(back, this, ctr, c));
    }

    private TextButton makeTopLabel(GameCanvas c) {
        BitmapFont font = SharedAssetContainer.getInstance().getUIFont(0.5f);
        TextButton.TextButtonStyle labelStyle = new TextButton.TextButtonStyle();
        labelStyle.font = font;
        labelStyle.fontColor = new Color(0.6f, 0.6f, 0.6f, 1);
        String text = "PRESS ESC TO CANCEL BINDING OR REMOVE MOD KEY";
        TextButton topLabel = new TextButton(text, labelStyle);
        topLabel.setSize(400, topLabel.getMaxHeight());
        topLabel.getLabel().setAlignment(Align.center);
        float ww = c.getTextViewport().getWorldWidth();
        float wh = c.getTextViewport().getWorldHeight();
        float xPosHeader = ww / 2f - topLabel.getWidth() / 2f;
        float yPos = wh * 0.925f;
        topLabel.setPosition(xPosHeader, yPos);
        return topLabel;
    }

    /**
     * Updates the controls labels so that they reflect the current controls.
     */
    public void updateControlsLabels() {
        for (MenuItem item : getItems()) {
            if (item instanceof ControlsMenuItem) {
                ((ControlsMenuItem) item).updateLabel();
            }
        }
    }

    @Override
    public List<TextButton> gatherLabels() {
        List<TextButton> labels = new ArrayList<>();
        for (MenuItem item : getItems()) {
            labels.add(item.getLabel());
            if (item instanceof ControlsMenuItem) {
                labels.add(((ControlsMenuItem) item).getHeaderLabel());
            }
        }
        labels.add(topLabel);
        return labels;
    }

}
