package com.thefirstlineofcode.sand.client.actuator;

import java.util.Properties;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.client.thing.ThingPlugin;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.actuator.oxm.ExecutionParserFactory;

public class ActuatorPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(ThingPlugin.class);
		chatSystem.registerParser(
				new IqProtocolChain(Execution.PROTOCOL),
				new ExecutionParserFactory());
		chatSystem.registerApi(IActuator.class, Actuator.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IActuator.class);
		chatSystem.unregisterParser(new IqProtocolChain(Execution.PROTOCOL));
		chatSystem.unregister(ThingPlugin.class);
	}

}
