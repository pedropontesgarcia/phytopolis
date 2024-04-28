package com.syndic8.phytopolis;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
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
        DisplayMode[] displayModes = Lwjgl3ApplicationConfiguration.getDisplayModes();
        List<DisplayMode> potentialDisplayModes = new ArrayList<Graphics.DisplayMode>(
                Arrays.asList(displayModes));
        List<DisplayMode> goodDisplayModes = new ArrayList<Graphics.DisplayMode>();
        config.setWindowPosition(-1, -1);
        config.setTitle("Phytopolis");
        config.useVsync(true);
        if (potentialDisplayModes.isEmpty()) {
            LOGGER.warning("No valid fullscreen resolutions were detected, " +
                                   "switching to compatibility (windowed) " +
                                   "mode.");
            config.setWindowedMode(1280, 720);
            config.setResizable(false);
        } else {
            potentialDisplayModes.sort(Comparator.comparingInt((DisplayMode dm) -> dm.refreshRate));
            int highestRefreshRate = potentialDisplayModes.get(
                    potentialDisplayModes.size() - 1).refreshRate;
            for (DisplayMode dm : potentialDisplayModes) {
                if (dm.refreshRate == highestRefreshRate) {
                    goodDisplayModes.add(dm);
                }
            }
            config.setFullscreenMode(goodDisplayModes.get(
                    goodDisplayModes.size() - 1));
        }
        new Lwjgl3Application(new GDXRoot(goodDisplayModes), config);
    }

}
