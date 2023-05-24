package com.thefirstlineofcode.sand.client.ibtr;

import java.util.Properties;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.protocols.ibtr.ThingRegister;
import com.thefirstlineofcode.sand.protocols.ibtr.oxm.ThingRegisterParserFactory;
import com.thefirstlineofcode.sand.protocols.ibtr.oxm.ThingRegisterTranslatorFactory;

public class InternalIbdrPlugin implements IPlugin {
	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(
				new IqProtocolChain(ThingRegister.PROTOCOL),
				new ThingRegisterParserFactory()
		);
		
		chatSystem.registerTranslator(
				ThingRegister.class,
				new ThingRegisterTranslatorFactory()
		);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(ThingRegister.class);
		
		chatSystem.unregisterParser(
				new IqProtocolChain(ThingRegister.PROTOCOL)
		);
	}
}
