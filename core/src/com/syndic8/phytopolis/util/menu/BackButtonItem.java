package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;

public class BackButtonItem extends MenuItem {

    public BackButtonItem(Menu back, Menu m, MenuContainer ctr, GameCanvas c) {
        super("< " + "BACK",
              m.getLength() - 1,
              back,
              m,
              ctr,
              c,
              Align.center,
              0.75f,
              -0.125f,
              0,
              200);
    }

}
