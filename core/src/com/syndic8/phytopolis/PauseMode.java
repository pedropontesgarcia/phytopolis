package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.ScreenListener;
import com.syndic8.phytopolis.util.menu.Menu;
import com.syndic8.phytopolis.util.menu.MenuContainer;
import com.syndic8.phytopolis.util.menu.MenuItem;
import com.syndic8.phytopolis.util.menu.OptionsMenu;

import static com.syndic8.phytopolis.GDXRoot.ExitCode;

public class PauseMode extends FadingScreen implements Screen {

    private final MenuContainer menuContainer;
    private final Menu menu;
    private ScreenListener listener;
    private boolean ready;
    private boolean exit;
    private boolean active;
    private GameCanvas canvas;
    private ExitCode exitCode;

    public PauseMode(GameCanvas c) {
        exit = false;
        ready = false;
        canvas = c;
        menu = new Menu(5, 0.125f);
        menuContainer = new MenuContainer(menu, c);
        Menu optionsMenu = new OptionsMenu(c, menuContainer, menu);
        ClickListener resumeListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitCode = ExitCode.EXIT_RESUME;
                exit = true;
                fadeOut();
            }
        };
        ClickListener resetListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitCode = ExitCode.EXIT_RESET;
                exit = true;
                fadeOut();
            }
        };
        ClickListener mainMenuListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitCode = ExitCode.EXIT_LEVELS;
                exit = true;
                fadeOut(0.5f);
            }
        };
        ClickListener exitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exit = true;
                exitCode = ExitCode.EXIT_QUIT;
            }
        };
        menu.addItem(new MenuItem("RESUME",
                                  0,
                                  resumeListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("OPTIONS",
                                  1,
                                  optionsMenu,
                                  menu,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("RESET LEVEL",
                                  2,
                                  resetListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("EXIT TO LEVELS",
                                  3,
                                  mainMenuListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("QUIT",
                                  4,
                                  exitListener,
                                  menu,
                                  menuContainer,
                                  canvas));
        menuContainer.populate();
    }

    public void fadeOut() {
        super.fadeOut(0.1f);
        menuContainer.deactivate();
    }

    public void setCanvas(GameCanvas c) {
        canvas = c;
    }

    @Override
    public void show() {
        exit = false;
        active = true;
        ready = false;
        exitCode = null;
        fadeIn(0.1f);
        menuContainer.activate();
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
        }

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void update(float delta) {
        super.update(delta);
        menuContainer.update(delta);
        InputController.getInstance().readInput();
        if (InputController.getInstance().didExit() &&
                menuContainer.getMenu() == menu) {
            exit = true;
            exitCode = ExitCode.EXIT_RESUME;
            fadeOut();

        }
        if (exit) {
            exit = false;
            ready = true;
            menuContainer.deactivate();
        }

        if ((ready && isFadeDone())) {
            listener.exitScreen(this, exitCode.ordinal());
        }
    }

    public void draw() {
        canvas.clear();
        canvas.begin();
        menuContainer.draw(canvas);
        canvas.end();
        super.draw(canvas);
    }

    public void setScreenListener(ScreenListener l) {
        listener = l;
    }

}
