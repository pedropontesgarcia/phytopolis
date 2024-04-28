package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.util.PooledList;

public class OptionsMenu extends Menu {

    public PooledList<ControlsMenuItem> controlsItems;

    public OptionsMenu(GameCanvas c, MenuContainer ctr, Menu back) {
        super(3, 0.125f);
        controlsItems = new PooledList<>();
        InputController ic = InputController.getInstance();
        Menu controlsMenu = new ControlsMenu(c, ctr, this);
        Menu graphicsMenu = new GraphicsMenu(c, ctr, this);
        addItem(new MenuItem("CONTROLS", 0, controlsMenu, this, ctr, c));
        addItem(new MenuItem("GRAPHICS", 1, graphicsMenu, this, ctr, c));
        addItem(new MenuItem("< BACK",
                             2,
                             back,
                             this,
                             ctr,
                             c,
                             Align.left,
                             0.75f));
    }

}
