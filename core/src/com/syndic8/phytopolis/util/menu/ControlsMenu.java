package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;

public class ControlsMenu extends Menu {

    public ControlsMenu(GameCanvas c, MenuContainer ctr, Menu back) {
        super(10, 0.075f, 0, 0, 0.75f, Align.left);
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
        addItem(new MenuItem("< BACK", 9, back, this, ctr, c));
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

}
