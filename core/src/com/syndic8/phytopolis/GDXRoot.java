/*
 * GDXRoot.java
 *
 * Authors: Syndic8
 */
package com.syndic8.phytopolis;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Screen;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.ScreenListener;

import java.util.List;

import static com.syndic8.phytopolis.GDXRoot.ExitCode.*;

/**
 * LibGDX's root class.
 */
public class GDXRoot extends Game implements ScreenListener {

    private final List<DisplayMode> displayModes;
    AssetDirectory directory;
    private GameCanvas canvas;
    private MainMenuMode menu;
    private LevelSelectMode levelSelect;
    private LevelOverMode levelOver;
    private PauseMode pause;
    private GameplayMode controller;

    public GDXRoot(List<DisplayMode> dms) {
        displayModes = dms;
    }

    public void create() {
        canvas = new GameCanvas(displayModes);
        menu = new MainMenuMode("assets.json", canvas, 15);
        controller = new GameplayMode(canvas);
        levelSelect = new LevelSelectMode(canvas);
        levelOver = new LevelOverMode(canvas, controller);
        pause = new PauseMode(canvas, controller);
        Gdx.input.setInputProcessor(InputController.getInstance()
                                            .getMultiplexer());
        menu.setScreenListener(this);
        setScreen(menu);
    }

    public void dispose() {
        // Call dispose on our children
        setScreen(null);
        menu.dispose();
        controller.dispose();
        levelSelect.dispose();
        levelOver.dispose();
        pause.dispose();
        canvas.dispose();
        canvas = null;

        // Unload all the resources
        if (directory != null) {
            directory.unloadAssets();
            directory.dispose();
            directory = null;
        }
        super.dispose();
    }

    @Override
    public void resume() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        canvas.resizeScreen(width, height);
    }

    /**
     * Exits the current screen and transfers control to the next one.
     *
     * @param screen   the screen requesting to exit.
     * @param exitCode the state of the screen upon exit.
     */
    public void exitScreen(Screen screen, int exitCode) {
        menu.resetCursor();
        if (screen == menu) {
            directory = menu.getAssets();
            levelSelect.gatherAssets(directory);
            levelSelect.setScreenListener(this);
            levelSelect.setBackgroundMusic(menu.getBackgroundMusic());
            setScreen(levelSelect);
        } else if (screen == levelSelect &&
                exitCode == EXIT_MAIN_MENU.ordinal()) {
            menu.setScreenListener(this);
            menu.setBackgroundMusic(levelSelect.getBackgroundMusic());
            setScreen(menu);
        } else if (screen == levelSelect && exitCode == EXIT_LEVELS.ordinal()) {
            controller.fadeOut(0);
            controller.setLevel(levelSelect.getLevel());
            controller.gatherAssets(directory);
            controller.reset();
            controller.setScreenListener(this);
            setScreen(controller);
        } else if (screen == levelOver) {
            controller.setPaused(false);
            levelSelect.reset();
            levelSelect.setScreenListener(this);
            setScreen(levelSelect);
        } else if (screen == pause && exitCode == EXIT_RESUME.ordinal()) {
            controller.setPaused(false);
            controller.setScreenListener(this);
            setScreen(controller);
        } else if (screen == pause && exitCode == EXIT_RESET.ordinal()) {
            controller.setPaused(false);
            controller.fadeOut(0);
            controller.reset();
            controller.setScreenListener(this);
            setScreen(controller);
        } else if (screen == pause && exitCode == EXIT_LEVELS.ordinal()) {
            controller.setPaused(false);
            levelSelect.reset();
            levelSelect.setScreenListener(this);
            levelSelect.setBackgroundMusic(menu.getBackgroundMusic());
            setScreen(levelSelect);
        } else if (exitCode == EXIT_VICTORY.ordinal()) {
            endLevel(true);
        } else if (exitCode == EXIT_FAILURE.ordinal()) {
            endLevel(false);
        } else if (exitCode == EXIT_PAUSE.ordinal()) {
            pause.setScreenListener(this);
            pause.setCanvas(canvas);
            setScreen(pause);
        } else if (exitCode == EXIT_QUIT.ordinal()) {
            Gdx.app.exit();
        }
    }

    private void endLevel(boolean won) {
        levelOver.setWon(won);
        levelOver.gatherAssets(directory);
        levelOver.reset();
        levelOver.setScreenListener(this);
        setScreen(levelOver);
    }

    /**
     * Exit codes for the screen listener.
     */
    public enum ExitCode {
        EXIT_QUIT,
        EXIT_VICTORY,
        EXIT_PAUSE,
        EXIT_FAILURE,
        EXIT_LEVELS,
        EXIT_RESUME,
        EXIT_RESET,
        EXIT_MAIN_MENU
    }

}
