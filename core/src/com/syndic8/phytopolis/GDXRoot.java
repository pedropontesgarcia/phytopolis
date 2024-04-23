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
    private VictoryScreen victory;
    /**
     * Player mode for the the game proper (CONTROLLER CLASS)
     */
    private int current;
    /**
     * WorldController
     */
    private WorldController controller;

    public GDXRoot() {
    }

    public void create() {
        canvas = new GameCanvas();
        canvas.setSize(16, 9);
        menu = new MainMenuMode("assets.json", canvas, 1);
        controller = new GameplayMode();
        levelSelect = new LevelSelectMode();
        victory = new VictoryScreen();
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
            levelSelect.setCanvas(canvas);
            levelSelect.setBackgroundMusic(menu.getBackgroundMusic());
            setScreen(levelSelect);
            menu.dispose();
            menu = null;
        } else if (screen == levelSelect) {
            //directory = menu.getAssets();
            ((GameplayMode) controller).setLevel(levelSelect.getLevel());
            controller.gatherAssets(directory);
            controller.setScreenListener(this);
            controller.setCanvas(canvas);
            controller.reset();
            setScreen(controller);
            //            menu.dispose();
            //            menu = null;
        } else if (screen == victory) {
            levelSelect.reset();
            setScreen(levelSelect);
        } else if (exitCode == WorldController.EXIT_VICTORY) {
            victory.reset();
            victory.gatherAssets(directory);
            victory.setScreenListener(this);
            victory.setCanvas(canvas);
            setScreen(victory);
        } else if (exitCode == WorldController.EXIT_QUIT) {
            // We quit the main application
            Gdx.app.exit();
        }
    }

}
