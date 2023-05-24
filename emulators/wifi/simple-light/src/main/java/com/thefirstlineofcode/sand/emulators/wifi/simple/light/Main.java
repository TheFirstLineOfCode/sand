package com.thefirstlineofcode.sand.emulators.wifi.simple.light;

import com.thefirstlineofcode.chalk.logger.LogConfigurator;
import com.thefirstlineofcode.chalk.logger.LogConfigurator.LogLevel;

public class Main {
	private static final String APP_NAME_SAND_WIFI_LIGHT = "sand-emulators-wifi-simple-ight";
	
	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
		new LogConfigurator().configure(APP_NAME_SAND_WIFI_LIGHT, LogLevel.DEBUG);
		new SimpleLightFrame().setVisible(true);
	}
}
