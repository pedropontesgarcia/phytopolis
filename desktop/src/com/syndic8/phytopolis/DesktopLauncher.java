package com.syndic8.phytopolis;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {

    public static void main(String[] arg) {
        //		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        //		config.setForegroundFPS(60);
        //		config.setTitle("Phytopolis");
        //		new Lwjgl3Application(new GDXRoot(), config);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // windowed fullscreen
        config.setWindowedMode( // windowed fullscreen
                Lwjgl3ApplicationConfiguration.getDisplayMode().width,
                Lwjgl3ApplicationConfiguration.getDisplayMode().height
        );
        // config.setWindowedMode(1920, 1080); // 1080p
        // config.setWindowedMode(1280, 720); // 720p
        // config.setWindowedMode(1920, 1200); // Mac 16:10
        // config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode()); // fullscreen
        config.setTitle("Phytopolis");
        new Lwjgl3Application(new GDXRoot(), config);
    }

}
