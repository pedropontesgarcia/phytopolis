/*
 * GDXRoot.java
 *
 * Authors: Syndic8
 */
package com.syndic8.phytopolis;

import com.badlogic.gdx.Game;
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

    public GDXRoot() {
    }

    public void create() {
        canvas = new GameCanvas();
    }

    public void dispose() {
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
