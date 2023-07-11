package com.thefirstlineofcode.sand.emulators.lora.gateway;

import com.thefirstlineofcode.chalk.logger.LogConfigurator;
import com.thefirstlineofcode.chalk.logger.LogConfigurator.LogLevel;
import com.thefirstlineofcode.sand.emulators.lora.network.LoraNetwork;

public class Main {
	private static final String APP_NAME_SAND_LORA_GATEWAY = "sand-emulators-lora-simple-gateway";

	public static void main(String[] args) {
		new Main().run();
	}
	
	private void run() {
		LogConfigurator.configure(APP_NAME_SAND_LORA_GATEWAY, LogLevel.DEBUG);
		
		Gateway gateway = new Gateway(new LoraNetwork());
		gateway.setVisible(true);
	}
}
