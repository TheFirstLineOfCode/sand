package com.thefirstlineofcode.sand.client.location;

import java.util.Properties;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.protocols.location.LocateThings;

public class LocationPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(new IqProtocolChain(LocateThings.PROTOCOL),
				new CocParserFactory<LocateThings>(LocateThings.class));
		chatSystem.registerTranslator(LocateThings.class,
				new CocTranslatorFactory<LocateThings>(LocateThings.class));
		chatSystem.registerApi(IThingLocator.class, ThingLocator.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IThingLocator.class);
		chatSystem.unregisterTranslator(LocateThings.class);
		chatSystem.unregisterParser(new IqProtocolChain(LocateThings.PROTOCOL));
	}

}
