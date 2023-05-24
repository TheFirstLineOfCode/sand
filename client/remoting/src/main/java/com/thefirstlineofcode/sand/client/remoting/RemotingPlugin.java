package com.thefirstlineofcode.sand.client.remoting;

import java.util.Properties;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.actuator.oxm.ExecutionParserFactory;
import com.thefirstlineofcode.sand.protocols.actuator.oxm.ExecutionTranslatorFactory;

public class RemotingPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(new IqProtocolChain(Execution.PROTOCOL),
				new ExecutionParserFactory());
		chatSystem.registerTranslator(Execution.class, new ExecutionTranslatorFactory());
		chatSystem.registerApi(IRemoting.class, Remoting.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IRemoting.class);
		chatSystem.unregisterTranslator(Execution.class);
		chatSystem.unregisterParser(new IqProtocolChain(Execution.PROTOCOL));
	}

}
