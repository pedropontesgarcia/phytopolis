package com.syndic8.phytopolis;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {

    private static final Logger LOGGER = Logger.getLogger(DesktopLauncher.class.getName());

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(Lwjgl3ApplicationConfiguration.getDisplayMode().width,
                               Lwjgl3ApplicationConfiguration.getDisplayMode().height);
        config.setTitle("Phytopolis");
        Graphics.DisplayMode[] displayModes = Lwjgl3ApplicationConfiguration.getDisplayModes();
        List<Graphics.DisplayMode> goodDisplayModes = new ArrayList<Graphics.DisplayMode>();
        for (Graphics.DisplayMode displayMode : displayModes) {
            if (Math.abs(displayMode.width / displayMode.height - 16 / 9) <
                    0.01) {
                goodDisplayModes.add(displayMode);
            }
        }
        goodDisplayModes.sort(Comparator.comparingInt((Graphics.DisplayMode dm) -> dm.width));
        // Uncomment this line to force windowed mode
        // goodDisplayModes.clear();
        if (goodDisplayModes.isEmpty()) {
            LOGGER.warning("No valid fullscreen resolutions were detected, " +
                                   "switching to compatibility (windowed) " +
                                   "mode.");
            config.setWindowedMode(1280, 720);
        } else config.setFullscreenMode(goodDisplayModes.get(
                goodDisplayModes.size() - 1));
        new Lwjgl3Application(new GDXRoot(), config);
    }

}
