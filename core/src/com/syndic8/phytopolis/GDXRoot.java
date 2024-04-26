/*
 * GDXRoot.java
 *
 * Authors: Syndic8
 */
package com.syndic8.phytopolis;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.util.ScreenListener;

/**
 * LibGDX's root class.
 */
public class GDXRoot extends Game implements ScreenListener {

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
    private VictoryMode victory;
    private PauseMode pause;
    private GameplayMode controller;

    public GDXRoot() {
    }

    public void create() {
        canvas = new GameCanvas();
        canvas.setSize(16, 9);
        menu = new MainMenuMode("assets.json", canvas, 1);
        controller = new GameplayMode();
        levelSelect = new LevelSelectMode(canvas);
        victory = new VictoryMode();
        pause = new PauseMode(canvas);
        Gdx.input.setInputProcessor(InputController.getInstance()
                                            .getMultiplexer());
        menu.setScreenListener(this);
        setScreen(menu);
    }

    public void dispose() {
        // Call dispose on our children
        setScreen(null);
        controller.dispose();
        levelSelect.dispose();
        victory.dispose();

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
                exitCode == LevelSelectMode.ExitCode.EXIT_MAIN_MENU.ordinal()) {
            menu.setScreenListener(this);
            menu.setBackgroundMusic(levelSelect.getBackgroundMusic());
            setScreen(menu);
        } else if (screen == levelSelect &&
                exitCode == LevelSelectMode.ExitCode.EXIT_LEVEL.ordinal()) {
            controller.setLevel(levelSelect.getLevel());
            controller.setCanvas(canvas);
            controller.gatherAssets(directory);
            controller.reset();
            controller.setScreenListener(this);
            controller.fadeIn(0.5f);
            setScreen(controller);
        } else if (screen == victory) {
            levelSelect.reset();
            setScreen(levelSelect);
        } else if (screen == pause &&
                exitCode == PauseMode.ExitCode.EXIT_RESUME.ordinal()) {
            controller.setScreenListener(this);
            controller.setPaused(false);
            controller.fadeIn(0.25f);
            setScreen(controller);
        } else if (screen == pause &&
                exitCode == PauseMode.ExitCode.EXIT_LEVELS.ordinal()) {
            controller.setPaused(false);
            levelSelect.reset();
            levelSelect.setScreenListener(this);
            levelSelect.setBackgroundMusic(menu.getBackgroundMusic());
            setScreen(levelSelect);
        } else if (exitCode ==
                WorldController.ExitCode.EXIT_VICTORY.ordinal()) {
            victory.gatherAssets(directory);
            victory.reset();
            victory.setScreenListener(this);
            victory.setCanvas(canvas);
            setScreen(victory);
        } else if (exitCode == WorldController.ExitCode.EXIT_PAUSE.ordinal()) {
            pause.setScreenListener(this);
            pause.setCanvas(canvas);
            setScreen(pause);
        } else if (exitCode == WorldController.ExitCode.EXIT_QUIT.ordinal()) {
            Gdx.app.exit();
        }
    }

}
