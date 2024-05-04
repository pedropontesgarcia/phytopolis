package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.util.FadingScreen;

/**
 * Container for a menu with items. Allows menu swapping (for sub/supermenus),
 * event listeners for menu items, and supports changing opacity. To be used
 * with Menus and MenuItems.
 */
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

    /**
     * Populates this MenuContainer. Should be called after all the items
     * have been added to the associated menu.
     */
    public void populate() {
        for (TextButton b : menu.gatherLabels()) {
            stage.addActor(b);
        }
    }

    /**
     * Activates input capturing for this MenuContainer. Should be called
     * when the screen that uses this MenuContainer takes focus.
     */
    public void activate() {
        InputController.getInstance().getMultiplexer().addProcessor(stage);
    }

    /**
     * Deactivates input capturing for this MenuContainer. SHould be called
     * when the screen that uses this MenuContainer loses focus.
     */
    public void deactivate() {
        InputController.getInstance().getMultiplexer().removeProcessor(stage);
    }

    /**
     * @return the menu currently associated to this MenuContainer.
     */
    public Menu getMenu() {
        return menu;
    }

    /**
     * Sets the menu associated to this MenuContainer.
     *
     * @param m the menu to associate.
     */
    public void setMenu(Menu m) {
        targetMenu = m;
        menuChangeScheduled = true;
        fadeOut(0.1f);
    }

    /**
     * Updates this MenuContainer, processing fades, input, menu changes, and
     * event listeners from the menu items.
     *
     * @param dt delta time.
     */
    public void update(float dt) {
        super.update(dt);
        if (menuChangeScheduled && isFadeDone()) {
            menuChangeScheduled = false;
            stage.clear();
            menu = targetMenu;
            for (TextButton b : menu.gatherLabels()) {
                stage.addActor(b);
            }
            if (menu instanceof SoundMenu) {
                for (Slider s : ((SoundMenu) menu).gatherSliders()) {
                    stage.addActor(s);
                }
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
            ((GraphicsMenu) menu).updateGraphicsLabels();
        }
        //        if (menu instanceof SoundMenu) {
        //            ((SoundMenu) menu).updateSoundLabels();
        //        }
    }

    /**
     * Draws this menu container.
     *
     * @param c game canvas.
     */
    public void draw(GameCanvas c) {
        stage.draw();
        super.draw(c);
    }

    /**
     * Sets the opacity of this menu.
     *
     * @param alpha alpha value.
     */
    public void setAlpha(float alpha) {
        for (Actor a : stage.getActors()) {
            a.getColor().a = alpha;
        }
    }

}
