package com.syndic8.phytopolis;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.util.OSUtils;

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
        List<DisplayMode> potentialDisplayModes = new ArrayList<DisplayMode>(
                Arrays.asList(displayModes));
        List<DisplayMode> goodDisplayModes = new ArrayList<DisplayMode>();
        potentialDisplayModes.sort(Comparator.comparingInt((DisplayMode dm) -> dm.refreshRate));
        int highestRefreshRate = potentialDisplayModes.get(
                potentialDisplayModes.size() - 1).refreshRate;
        for (DisplayMode dm : potentialDisplayModes) {
            if (dm.refreshRate == highestRefreshRate) {
                goodDisplayModes.add(dm);
            }
        }
        manageSettings(config, goodDisplayModes);
        new Lwjgl3Application(new GDXRoot(goodDisplayModes), config);
    }

    private static void manageSettings(Lwjgl3ApplicationConfiguration config,
                                       List<DisplayMode> displayModes) {
        FileHandle configFile = new Lwjgl3FileHandle(OSUtils.getConfigFile(),
                                                     Files.FileType.Absolute);
        try {
            JsonReader settingsJsonReader = new JsonReader();
            JsonValue settingsJson = settingsJsonReader.parse(configFile);
            boolean windowed = settingsJson.getBoolean("windowed");
            int resolutionIndex = settingsJson.getInt("resolutionIndex");
            DisplayMode resolution = displayModes.get(resolutionIndex);
            int windowWidth = (int) (
                    Lwjgl3ApplicationConfiguration.getDisplayMode().width *
                            0.8f);
            int windowHeight = (int) (
                    (Lwjgl3ApplicationConfiguration.getDisplayMode().width *
                            0.8f * 16f) / 9f);
            int[] fps = new int[]{0, 15, 30, 45, 60, 90, 120};
            int fpsIndex = settingsJson.getInt("fpsIndex");
            int currentFps = fps[fpsIndex];

            if (!windowed) config.setFullscreenMode(resolution);
            else config.setWindowedMode(windowWidth, windowHeight);
            config.setForegroundFPS(currentFps);
            config.useVsync(currentFps == 0);
            config.setTitle("Phytopolis");
            config.setWindowPosition(-1, -1);
            config.setResizable(false);
        } catch (Exception ignored) {
            resetSettings(configFile);
            // Try again
            manageSettings(config, displayModes);
        }
    }

    private static void resetSettings(FileHandle configFile) {
        FileHandle defaultConfigFile = new Lwjgl3FileHandle(
                "defaultSettings.json",
                Files.FileType.Internal);
        defaultConfigFile.copyTo(configFile);
    }

}
