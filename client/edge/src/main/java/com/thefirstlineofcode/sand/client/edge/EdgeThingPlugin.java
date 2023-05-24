package com.thefirstlineofcode.sand.client.edge;

import java.util.Properties;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.client.actuator.ActuatorPlugin;
import com.thefirstlineofcode.sand.protocols.edge.Restart;
import com.thefirstlineofcode.sand.protocols.edge.ShutdownSystem;
import com.thefirstlineofcode.sand.protocols.edge.Stop;

public class EdgeThingPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(ActuatorPlugin.class);
		chatSystem.registerParser(new IqProtocolChain(Stop.PROTOCOL),
				new CocParserFactory<>(Stop.class));
		chatSystem.registerTranslator(Stop.class,
				new CocTranslatorFactory<>(Stop.class));
		chatSystem.registerParser(new IqProtocolChain(Restart.PROTOCOL),
				new CocParserFactory<>(Restart.class));
		chatSystem.registerTranslator(Restart.class,
				new CocTranslatorFactory<>(Restart.class));
		chatSystem.registerParser(new IqProtocolChain(ShutdownSystem.PROTOCOL),
				new CocParserFactory<>(ShutdownSystem.class));
		chatSystem.registerTranslator(ShutdownSystem.class,
				new CocTranslatorFactory<>(ShutdownSystem.class));
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(ShutdownSystem.class);
		chatSystem.unregisterParser(new IqProtocolChain(ShutdownSystem.PROTOCOL));
		chatSystem.unregisterTranslator(Restart.class);		
		chatSystem.unregisterParser(new IqProtocolChain(Restart.PROTOCOL));
		chatSystem.unregisterTranslator(Stop.class);
		chatSystem.unregisterParser(new IqProtocolChain(Stop.PROTOCOL));
		chatSystem.unregister(ActuatorPlugin.class);
	}

}
