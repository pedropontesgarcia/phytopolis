package com.syndic8.phytopolis;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.syndic8.phytopolis.util.OSUtils;
import edu.cornell.gdiac.backend.GDXApp;
import edu.cornell.gdiac.backend.GDXAppSettings;
import lwjgl3.Lwjgl3ApplicationConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument

/**
 * Launcher class for desktop mode.
 */
public class DesktopLauncher {

    public static void main(String[] arg) {
        GDXAppSettings config = new GDXAppSettings();
        config.width = Lwjgl3ApplicationConfiguration.getDisplayMode().width;
        config.height = Lwjgl3ApplicationConfiguration.getDisplayMode().height;
        config.title = "Phytopolis";
        config.getLwjgl3Configuration()
                .setWindowIcon("icons/icon.png",
                               "icons/icon2.png",
                               "icons/icon3.png",
                               "icons/icon4.png");
        config.getLwjgl3Configuration()
                .setWindowedMode(Lwjgl3ApplicationConfiguration.getDisplayMode().width,
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
        manageSettings(config.getLwjgl3Configuration(), goodDisplayModes);
        manageSave();
        new GDXApp(new GDXRoot(goodDisplayModes), config);
    }

    private static void manageSettings(lwjgl3.Lwjgl3ApplicationConfiguration config,
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
                settingsJson.has("masterVolume") &&
                settingsJson.has("musicVolume") &&
                settingsJson.has("fxVolume"))) {
            throw new IOException();
        }
    }

    private static void resetSettings(FileHandle configFile) {
        FileHandle defaultConfigFile = new Lwjgl3FileHandle(
                "defaultSettings.json",
                Files.FileType.Internal);
        defaultConfigFile.copyTo(configFile);
    }

    private static void manageSave() {
        FileHandle saveFile = new Lwjgl3FileHandle(OSUtils.getSaveFile(),
                                                   Files.FileType.Absolute);
        try {
            JsonReader saveJsonReader = new JsonReader();
            JsonValue saveJson = saveJsonReader.parse(saveFile);
            ensureSaveExist(saveJson);
        } catch (Exception ignored) {
            resetSave(saveFile);
            // Try again
            manageSave();
        }
    }

    private static void ensureSaveExist(JsonValue saveJson) throws IOException {
        if (!(saveJson.has("lastBeaten"))) {
            throw new IOException();
        }
        for (int i = 1; i <= 12; i++) {
            if (!(saveJson.has("bestTime" + i))) {
                throw new IOException();
            }
        }
    }

    private static void resetSave(FileHandle saveFile) {
        FileHandle defaultSaveFile = new Lwjgl3FileHandle("defaultSave.json",
                                                          Files.FileType.Internal);
        defaultSaveFile.copyTo(saveFile);
    }

}
