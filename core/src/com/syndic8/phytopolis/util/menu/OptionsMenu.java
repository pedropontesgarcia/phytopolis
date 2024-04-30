package com.syndic8.phytopolis.util.menu;

import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.util.PooledList;

public class OptionsMenu extends Menu {

    public PooledList<ControlsMenuItem> controlsItems;

    public OptionsMenu(GameCanvas c, MenuContainer ctr, Menu back) {
        super(4, 0.125f);
        controlsItems = new PooledList<>();
        InputController ic = InputController.getInstance();
        Menu controlsMenu = new ControlsMenu(c, ctr, this);
        Menu graphicsMenu = new GraphicsMenu(c, ctr, this);
        Menu soundMenu = new SoundMenu(c, ctr, this);
        addItem(new MenuItem("CONTROLS", 0, controlsMenu, this, ctr, c));
        addItem(new MenuItem("GRAPHICS", 1, graphicsMenu, this, ctr, c));
        addItem(new MenuItem("SOUND", 2, soundMenu, this, ctr, c));
        addItem(new BackButtonItem(back, this, ctr, c));
    }

}
