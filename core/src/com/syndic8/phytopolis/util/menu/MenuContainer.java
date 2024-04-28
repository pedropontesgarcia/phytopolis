package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.util.FadingScreen;

public class MenuContainer extends FadingScreen {

    private final Stage stage;
    private boolean menuChangeScheduled;
    private Menu menu;
    private Menu targetMenu;

    public MenuContainer(Menu m, GameCanvas c) {
        menu = m;
        stage = new Stage(c.getTextViewport());
        menuChangeScheduled = false;
    }

    public void populate() {
        for (TextButton b : menu.gatherLabels()) {
            stage.addActor(b);
        }
    }

    public void activate() {
        InputController.getInstance().getMultiplexer().addProcessor(stage);
    }

    public void deactivate() {
        InputController.getInstance().getMultiplexer().removeProcessor(stage);
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu m) {
        targetMenu = m;
        menuChangeScheduled = true;
        fadeOut(0.1f);
    }

    public void update(float dt) {
        super.update(dt);
        if (menuChangeScheduled && isFadeDone()) {
            menuChangeScheduled = false;
            stage.clear();
            menu = targetMenu;
            for (TextButton b : menu.gatherLabels()) {
                stage.addActor(b);
            }
            fadeIn(0.1f);
        }
        if (!InputController.getInstance().isUpdateScheduled()) {
            stage.act();
        }
        if (menu instanceof ControlsMenu) {
            ((ControlsMenu) menu).updateControlsLabels();
        }
        if (menu instanceof GraphicsMenu) {
            ((GraphicsMenu) menu).updateGraphicssLabels();
        }
    }

    public void draw(GameCanvas c) {
        stage.draw();
        super.draw(c);
    }

}
