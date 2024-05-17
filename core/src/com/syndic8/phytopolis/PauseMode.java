package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.ScreenListener;
import com.syndic8.phytopolis.util.SharedAssetContainer;
import com.syndic8.phytopolis.util.menu.Menu;
import com.syndic8.phytopolis.util.menu.MenuContainer;
import com.syndic8.phytopolis.util.menu.MenuItem;
import com.syndic8.phytopolis.util.menu.OptionsMenu;

import static com.syndic8.phytopolis.GDXRoot.ExitCode;

public class PauseMode extends FadingScreen implements Screen {

    private final MenuContainer menuContainer;
    private final Menu menu;
    private final GameplayMode gameplayMode;
    private final float BLACK_BACKGROUND_ALPHA = 0.6f;
    private final float TRANSITION_DURATION = 0.1f;
    private float alpha;
    private ScreenListener listener;
    private boolean ready;
    private boolean exit;
    private boolean active;
    private GameCanvas canvas;
    private ExitCode exitCode;
    private float tmr;

    public PauseMode(GameCanvas c, GameplayMode gm) {
        exit = false;
        ready = false;
        canvas = c;
        gameplayMode = gm;
        alpha = 0;
        tmr = 0;
        menu = new Menu(5, 0.125f);
        menuContainer = new MenuContainer(menu, c);
        Menu optionsMenu = new OptionsMenu(c, menuContainer, menu);
        ClickListener resumeListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                exitCode = ExitCode.EXIT_RESUME;
                exit = true;
            }
        };
        ClickListener resetListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                exitCode = ExitCode.EXIT_RESET;
                exit = true;
                fadeOut(0.5f);
            }
        };
        ClickListener mainMenuListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                exitCode = ExitCode.EXIT_LEVELS;
                exit = true;
                fadeOut(0.5f);
            }
        };
        ClickListener exitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundController.getInstance()
                        .playSound(SharedAssetContainer.getInstance()
                                           .getSound("click"));
                exitCode = ExitCode.EXIT_QUIT;
                exit = true;
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

    public void setCanvas(GameCanvas c) {
        canvas = c;
    }

    @Override
    public void show() {
        exit = false;
        active = true;
        ready = false;
        exitCode = null;
        tmr = 0;
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
        InputController.getInstance().readInput();
        if (InputController.getInstance().didExit() &&
                menuContainer.getMenu() == menu) {
            exit = true;
            exitCode = ExitCode.EXIT_RESUME;
        }
        super.update(delta);
        tmr += delta;
        if (exit && exitCode == ExitCode.EXIT_RESUME) {
            tmr = 0;
        }
        if (tmr >= TRANSITION_DURATION) {
            tmr = TRANSITION_DURATION;
        }
        alpha = (exitCode == ExitCode.EXIT_RESUME ?
                1 - tmr / TRANSITION_DURATION :
                tmr / TRANSITION_DURATION);
        menuContainer.update(delta);
        menuContainer.setAlpha(alpha);
        float volume = SoundController.getInstance().getUserMusicVolume() *
                (1f - (alpha) / 1.25f);
        SoundController.getInstance().setActualMusicVolume(volume);
        if (exit) {
            exit = false;
            ready = true;
            menuContainer.deactivate();
        }
        if (ready && ((exitCode == ExitCode.EXIT_RESUME &&
                tmr == TRANSITION_DURATION) ||
                (exitCode != ExitCode.EXIT_RESUME && isFadeDone()) ||
                exitCode == ExitCode.EXIT_QUIT)) {
            listener.exitScreen(this, exitCode.ordinal());
        }
    }

    public void draw() {
        canvas.clear();
        if (menuContainer.getMenu() == menu) gameplayMode.draw();
        canvas.beginShape();
        canvas.getShapeRenderer()
                .setColor(0, 0, 0, alpha * BLACK_BACKGROUND_ALPHA);
        canvas.getShapeRenderer()
                .rect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.endShape();
        canvas.begin();
        menuContainer.draw(canvas);
        canvas.end();
        super.draw(canvas);
    }

    public void setScreenListener(ScreenListener l) {
        listener = l;
    }

}
