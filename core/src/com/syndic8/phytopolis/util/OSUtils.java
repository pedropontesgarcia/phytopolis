package com.syndic8.phytopolis.util;

import static com.badlogic.gdx.scenes.scene2d.utils.UIUtils.*;

/**
 * Credit to Alfred Reibenschuh, 2021.
 * <a href="https://github.com/libgdx/libgdx/issues/6559#issuecomment-890307952">Original code</a>
 */
public final class OSUtils {

    public static String getConfigFile() {
        return getConfigDirectory() + "/settings.json";
    }

    /**
     * Checks the OS type and returns the configuration directory.
     *
     * @return The configuration directory to save user settings.
     */
    public static String getConfigDirectory() {
        String CONFIG_HOME = System.getenv("XDG_CONFIG_HOME");

        if (CONFIG_HOME == null) {
            if (isLinux) {
                CONFIG_HOME = System.getProperty("user.home") + "/.config";
            } else if (isMac) {
                CONFIG_HOME = System.getProperty("user.home") +
                        "/Library/Preferences";
            } else if (isWindows) {
                CONFIG_HOME = System.getenv("APPDATA");
                if (CONFIG_HOME == null) {
                    CONFIG_HOME =
                            System.getProperty("user.home") + "/Local Settings";
                }
            }
        }
        return CONFIG_HOME + "/Phytopolis";
    }

    public static String getSaveFile() {
        return getDataDirectory() + "/saves.json";
    }

    /**
     * Checks the OS type and returns the data directory.
     *
     * @return The data directory to save user data.
     */
    public static String getDataDirectory() {
        String DATA_HOME = System.getenv("XDG_DATA_HOME");

        if (DATA_HOME == null) {
            if (isLinux) {
                DATA_HOME = System.getProperty("user.home") + "/.local/share";
            } else if (isMac) {
                DATA_HOME = System.getProperty("user.home") +
                        "/Library/Application Support";
            } else if (isWindows) {
                DATA_HOME = System.getenv("APPDATA");
                if (DATA_HOME == null) {
                    DATA_HOME = System.getProperty("user.home") +
                            "/Local Settings/Application Data";
                }
            }
        }
        return DATA_HOME + "/Phytopolis";
    }

}
