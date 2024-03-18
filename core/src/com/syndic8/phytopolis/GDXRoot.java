/*
 * GDXRoot.java
 *
 * Authors: Syndic8
 */
package com.syndic8.phytopolis;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.syndic8.phytopolis.assets.AssetDirectory;
import com.syndic8.phytopolis.level.PlatformController;
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
        menu = new MainMenuMode("assets.json", canvas, 1);
        controller = new PlatformController();
        menu.setScreenListener(this);
        setScreen(menu);
    }

    public void dispose() {
        // Call dispose on our children
        setScreen(null);
        controller.dispose();

        canvas.dispose();
        canvas = null;

        // Unload all of the resources
        if (directory != null) {
            directory.unloadAssets();
            directory.dispose();
            directory = null;
        }
        super.dispose();
    }

    public void resize(int width, int height) {
    }

    public void exitScreen(Screen screen, int exitCode) {
    }

}
