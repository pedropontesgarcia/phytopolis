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
import com.badlogic.gdx.graphics.Cursor;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.ScreenListener;

import java.util.List;

import static com.syndic8.phytopolis.GDXRoot.ExitCode.*;

/**
 * LibGDX's root class.
 */
public class GDXRoot extends Game implements ScreenListener {

    private final List<DisplayMode> displayModes;
    /**
     * Directory for game assets.
     */
    AssetDirectory directory;
    /**
     * Drawing context to display graphics (view).
     */
    private GameCanvas canvas;
    /**
     * Player mode for the asset loading screen (CONTROLLER CLASS)
     */
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
        canvas.setSize(16, 9);
        menu = new MainMenuMode("assets.json", canvas, 1);
        controller = new GameplayMode();
        levelSelect = new LevelSelectMode(canvas);
        levelOver = new LevelOverMode(canvas);
        pause = new PauseMode(canvas);
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
        canvas.resizeScreen(Gdx.graphics.getBackBufferWidth(),
                            Gdx.graphics.getBackBufferHeight());
    }

    public void resize(int width, int height) {
        canvas.resizeScreen(width, height);
    }

    public void exitScreen(Screen screen, int exitCode) {
        if (screen == menu) {
            directory = menu.getAssets();
            levelSelect.gatherAssets(directory);
            levelSelect.setScreenListener(this);
            levelSelect.setBackgroundMusic(menu.getBackgroundMusic());
            setScreen(levelSelect);
        } else if (screen == levelSelect &&
                exitCode == EXIT_MAIN_MENU.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            menu.setScreenListener(this);
            menu.setBackgroundMusic(levelSelect.getBackgroundMusic());
            setScreen(menu);
        } else if (screen == levelSelect && exitCode == EXIT_LEVELS.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            controller.setLevel(levelSelect.getLevel());
            controller.setCanvas(canvas);
            controller.gatherAssets(directory);
            controller.reset();
            controller.setScreenListener(this);
            controller.fadeIn(0.5f);
            setScreen(controller);
        } else if (screen == levelOver) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            controller.setPaused(false);
            levelSelect.reset();
            levelSelect.setScreenListener(this);
            setScreen(levelSelect);
        } else if (screen == pause && exitCode == EXIT_RESUME.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            controller.setScreenListener(this);
            controller.setPaused(false);
            controller.fadeIn(0.25f);
            setScreen(controller);
        } else if (screen == pause && exitCode == EXIT_RESET.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            controller.reset();
            controller.setScreenListener(this);
            controller.setPaused(false);
            controller.fadeIn(0.5f);
            setScreen(controller);
        } else if (screen == pause && exitCode == EXIT_LEVELS.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            controller.setPaused(false);
            levelSelect.reset();
            levelSelect.setScreenListener(this);
            levelSelect.setBackgroundMusic(menu.getBackgroundMusic());
            setScreen(levelSelect);
        } else if (exitCode == EXIT_VICTORY.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            endLevel(true);
        } else if (exitCode == EXIT_FAILURE.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            endLevel(false);
        } else if (exitCode == EXIT_PAUSE.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            pause.setScreenListener(this);
            pause.setCanvas(canvas);
            setScreen(pause);
        } else if (exitCode == EXIT_QUIT.ordinal()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
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
