package com.mygdx.game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import mygdx.cells.Cells;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher{
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		//config.setForegroundFPS(15);
		//config.useVsync(true);
		config.setTitle("cells");
		config.setWindowedMode(1280, 720);
		config.setFullscreenMode(null);
		config.setResizable(true);
		new Lwjgl3Application(new Cells(), config);
	}
}
