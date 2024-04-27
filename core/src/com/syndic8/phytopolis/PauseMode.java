package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.ScreenListener;
import com.syndic8.phytopolis.util.menu.Menu;
import com.syndic8.phytopolis.util.menu.MenuContainer;
import com.syndic8.phytopolis.util.menu.MenuItem;

import static com.syndic8.phytopolis.WorldController.ExitCode;

public class PauseMode extends FadingScreen implements Screen {

    private final Rectangle bounds;
    private final MenuContainer menuContainer;
    private ScreenListener listener;
    private boolean ready;
    private boolean exit;
    private boolean active;
    private GameCanvas canvas;
    private ExitCode exitCode;

    public PauseMode(GameCanvas c) {
        exit = false;
        ready = false;
        bounds = new Rectangle(0, 0, 16, 9);
        canvas = c;
        Menu menu = new Menu(4, 0.15f);
        Menu submenu = new Menu(2, 0.15f);
        menuContainer = new MenuContainer(menu, c);
        ClickListener resumeListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
//                exit = true;
                exitCode = ExitCode.EXIT_RESUME;
                exit = true;
                fadeOut();
            }
        };
        ClickListener mainMenuListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
//                exit = true;
                exitCode = ExitCode.EXIT_LEVELS;
                exit = true;
                fadeOut();
            }
        };
        ClickListener exitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
//                Gdx.app.exit();
//                exit = true;
                menuContainer.deactivate();
                exit = true;
                exitCode = ExitCode.EXIT_QUIT;
//                fadeOut();
            }
        };
        menu.addItem(new MenuItem("RESUME",
                                  menu.getSeparation(),
                                  0,
                                  menu.getLength(),
                                  resumeListener,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("OPTIONS",
                                  menu.getSeparation(),
                                  1,
                                  menu.getLength(),
                                  submenu,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("EXIT TO LEVELS",
                                  menu.getSeparation(),
                                  2,
                                  menu.getLength(),
                                  mainMenuListener,
                                  menuContainer,
                                  canvas));
        menu.addItem(new MenuItem("QUIT",
                                  menu.getSeparation(),
                                  3,
                                  menu.getLength(),
                                  exitListener,
                                  menuContainer,
                                  canvas));
        submenu.addItem(new MenuItem("QUIT",
                                     submenu.getSeparation(),
                                     0,
                                     submenu.getLength(),
                                     exitListener,
                                     menuContainer,
                                     canvas));
        submenu.addItem(new MenuItem("BACK",
                                     submenu.getSeparation(),
                                     1,
                                     submenu.getLength(),
                                     menu,
                                     menuContainer,
                                     canvas));
        menuContainer.populate();
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
        InputController.getInstance().readInput(bounds, Vector2.Zero.add(1, 1));
        if (InputController.getInstance().didExit()) {
//            exit = true;
//            System.out.println("X");
            exitCode = ExitCode.EXIT_RESUME;
            fadeOut();

        }
        if (exit) {
            exit = false;
            ready = true;
//            fadeOut(0.1f);
//            menuContainer.deactivate();
        }

        if ((ready && isFadeDone())) {
            resetFade();
            listener.exitScreen(this, exitCode.ordinal());
        }
    }

    public void fadeOut() {
        super.fadeOut(0.1f);
        menuContainer.deactivate();
    }

    public void draw() {
        canvas.clear();
        menuContainer.draw(canvas);
        super.draw(canvas);
    }

    public void setScreenListener(ScreenListener l) {
        listener = l;
    }

//    public enum ExitCode {EXIT_LEVELS, EXIT_RESUME}

}
