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
        config.setWindowedMode(Lwjgl3ApplicationConfiguration.getDisplayMode().width, Lwjgl3ApplicationConfiguration.getDisplayMode().height);
        // fullscreen
        // config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        config.setTitle("Phytopolis");
        new Lwjgl3Application(new GDXRoot(), config);
    }

}
