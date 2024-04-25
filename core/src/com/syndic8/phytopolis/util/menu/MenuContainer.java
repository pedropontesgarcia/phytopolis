package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.syndic8.phytopolis.GameCanvas;

public class MenuContainer {

    private final Stage stage;
    private final Menu menu;

    public MenuContainer(Menu m, GameCanvas c) {
        menu = m;
        stage = new Stage(c.getTextViewport());
        for (TextButton b : m.gatherLabels()) {
            stage.addActor(b);
        }

    }

    public Stage getStage() {
        return stage;
    }

    public void update() {
        System.out.println("ACTING");
        stage.act();
    }

    public void draw() {
        stage.draw();
    }

}
