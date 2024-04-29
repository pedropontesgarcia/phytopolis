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
        int windowWidth = (int) (
                Lwjgl3ApplicationConfiguration.getDisplayMode().width * 0.8f);
        int windowHeight = (int) (
                (Lwjgl3ApplicationConfiguration.getDisplayMode().width * 0.8f *
                        16f) / 9f);
        config.setTitle("Phytopolis");
        config.setWindowPosition(-1, -1);
        config.setWindowedMode(windowWidth, windowHeight);
        config.setResizable(false);
        potentialDisplayModes.sort(Comparator.comparingInt((DisplayMode dm) -> dm.refreshRate));
        int highestRefreshRate = potentialDisplayModes.get(
                potentialDisplayModes.size() - 1).refreshRate;
        for (DisplayMode dm : potentialDisplayModes) {
            if (dm.refreshRate == highestRefreshRate) {
                goodDisplayModes.add(dm);
            }
        }
        new Lwjgl3Application(new GDXRoot(goodDisplayModes), config);
    }

}
