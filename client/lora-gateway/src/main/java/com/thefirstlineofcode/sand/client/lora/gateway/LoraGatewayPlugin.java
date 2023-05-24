package com.thefirstlineofcode.sand.client.lora.gateway;

import java.util.Properties;

import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.client.concentrator.ConcentratorPlugin;
import com.thefirstlineofcode.sand.client.lora.dac.LoraDacPlugin;

public class LoraGatewayPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(LoraDacPlugin.class);
		chatSystem.register(ConcentratorPlugin.class);
		
		chatSystem.registerApi(ILoraGateway.class, LoraGateway.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(ILoraGateway.class);
		
		chatSystem.unregister(ConcentratorPlugin.class);
		chatSystem.unregister(LoraDacPlugin.class);
	}

}
