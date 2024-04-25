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

public class PauseMode extends FadingScreen implements Screen {

    private final Rectangle bounds;
    private final MenuContainer menuContainer;
    private ScreenListener listener;
    private boolean ready;
    private boolean active;
    private GameCanvas canvas;
    private ExitCode exitCode;

    public PauseMode(GameCanvas c) {
        ready = false;
        bounds = new Rectangle(0, 0, 16, 9);
        canvas = c;
        Menu menu = new Menu(3, 150f);
        ClickListener resumeListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ready = true;
                fadeOut(0.1f);
                InputController.getInstance()
                        .getMultiplexer()
                        .removeProcessor(menuContainer.getStage());
                exitCode = ExitCode.EXIT_RESUME;
            }
        };
        ClickListener mainMenuListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ready = true;
                fadeOut(0.1f);
                InputController.getInstance()
                        .getMultiplexer()
                        .removeProcessor(menuContainer.getStage());
                exitCode = ExitCode.EXIT_MAIN_MENU;
            }
        };
        ClickListener exitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        };
        menu.addItem(new MenuItem("RESUME",
                                  menu.getSeparation(),
                                  0,
                                  menu.getLength(),
                                  resumeListener,
                                  canvas));
        menu.addItem(new MenuItem("EXIT TO MAIN MENU",
                                  menu.getSeparation(),
                                  1,
                                  menu.getLength(),
                                  mainMenuListener,
                                  canvas));
        menu.addItem(new MenuItem("QUIT",
                                  menu.getSeparation(),
                                  2,
                                  menu.getLength(),
                                  exitListener,
                                  canvas));
        menuContainer = new MenuContainer(menu, c);
    }

    public void setCanvas(GameCanvas c) {
        canvas = c;
    }

    @Override
    public void show() {
        active = true;
        ready = false;
        fadeIn(0.1f);
        InputController.getInstance()
                .getMultiplexer()
                .addProcessor(menuContainer.getStage());
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (listener != null && ready && isFadeDone()) {
                listener.exitScreen(this, exitCode.ordinal());
            }
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
        InputController.getInstance().readInput(bounds, Vector2.Zero.add(1, 1));
        if (InputController.getInstance().didExit() && !ready) {
            ready = true;
            fadeOut(0.1f);
            InputController.getInstance()
                    .getMultiplexer()
                    .removeProcessor(menuContainer.getStage());
            exitCode = ExitCode.EXIT_RESUME;
        }
        menuContainer.update();
    }

    public void draw() {
        canvas.clear();
        menuContainer.draw();
        super.draw(canvas);
    }

    public void setScreenListener(ScreenListener l) {
        listener = l;
    }

    public enum ExitCode {EXIT_MAIN_MENU, EXIT_RESUME}

}
