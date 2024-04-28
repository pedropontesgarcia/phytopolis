package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.InputController.Binding;

public class ControlsMenuItem extends MenuItem {

    private final Binding binding;
    private final String header;

    public ControlsMenuItem(String text,
                            int index,
                            Menu m,
                            MenuContainer ctr,
                            GameCanvas c,
                            Binding b) {
        super(text, index, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                InputController.getInstance().updateBinding(b);
            }
        }, m, ctr, c);
        header = text;
        binding = b;
    }

    public void updateLabel() {
        getLabel().setText(header + ":  " + InputController.getInstance()
                .getBindingString(binding));
    }

}
