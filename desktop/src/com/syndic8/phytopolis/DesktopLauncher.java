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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(Lwjgl3ApplicationConfiguration.getDisplayMode().width,
                               Lwjgl3ApplicationConfiguration.getDisplayMode().height);
        DisplayMode[] displayModes = Lwjgl3ApplicationConfiguration.getDisplayModes();
        List<DisplayMode> potentialDisplayModes = new ArrayList<>(Arrays.asList(
                displayModes));
        List<DisplayMode> goodDisplayModes = new ArrayList<>();
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
            ensureSettingsExist(settingsJson);
            boolean windowed = settingsJson.getBoolean("windowed");
            int resolutionIndex = settingsJson.getInt("resolutionIndex");
            if (resolutionIndex == -1)
                resolutionIndex = displayModes.size() - 1;
            DisplayMode resolution = displayModes.get(resolutionIndex);
            int windowWidth = settingsJson.getInt("windowWidth");
            int windowHeight = settingsJson.getInt("windowHeight");
            int[] fps = new int[]{0, 15, 30, 45, 60, 90, 120};
            int fpsIndex = settingsJson.getInt("fpsIndex");
            int currentFps = fps[fpsIndex];

            if (!windowed) config.setFullscreenMode(resolution);
            else config.setWindowedMode(windowWidth, windowHeight);
            config.setForegroundFPS(currentFps);
            config.useVsync(currentFps == 0);
            config.setTitle("Phytopolis");
            config.setWindowPosition(-1, -1);
            config.setWindowIcon("ui/leaf-cursor.png");
        } catch (Exception ignored) {
            resetSettings(configFile);
            // Try again
            manageSettings(config, displayModes);
        }
    }

    private static void ensureSettingsExist(JsonValue settingsJson)
            throws IOException {
        if (!(settingsJson.has("jumpKey") && settingsJson.has("leftKey") &&
                settingsJson.has("rightKey") && settingsJson.has("dropKey") &&
                settingsJson.has("growBranchButton") &&
                settingsJson.has("growBranchModKey") &&
                settingsJson.has("growLeafButton") &&
                settingsJson.has("growLeafModKey") &&
                settingsJson.has("resolutionIndex") &&
                settingsJson.has("fpsIndex") && settingsJson.has("windowed") &&
                settingsJson.has("windowWidth") &&
                settingsJson.has("windowHeight") &&
                settingsJson.has("masterVolumeIndex") &&
                settingsJson.has("musicVolumeIndex") &&
                settingsJson.has("fxVolumeIndex"))) {
            throw new IOException();
        }
    }

    private static void resetSettings(FileHandle configFile) {
        FileHandle defaultConfigFile = new Lwjgl3FileHandle(
                "defaultSettings.json",
                Files.FileType.Internal);
        defaultConfigFile.copyTo(configFile);
    }

}
