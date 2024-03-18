package com.syndic8.phytopolis;

import edu.cornell.gdiac.backend.GDXApp;
import edu.cornell.gdiac.backend.GDXAppSettings;
import com.syndic8.phytopolis.GDXRoot;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
//		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
//		config.setForegroundFPS(60);
//		config.setTitle("Phytopolis");
//		new Lwjgl3Application(new GDXRoot(), config);

		GDXAppSettings config = new GDXAppSettings();
		config.width = 500;
		config.height = 800;
		config.fullscreen = false;
		config.resizable = false;
		config.title = "Phytopolis";
		new GDXApp(new GDXRoot(), config);
	}
}
