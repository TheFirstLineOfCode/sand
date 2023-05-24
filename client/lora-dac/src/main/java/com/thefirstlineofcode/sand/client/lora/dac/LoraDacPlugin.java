package com.thefirstlineofcode.sand.client.lora.dac;

import java.util.Properties;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.protocols.lora.dac.Reconfigure;
import com.thefirstlineofcode.sand.protocols.lora.dac.ResetLoraDacService;

public class LoraDacPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(new IqProtocolChain(ResetLoraDacService.PROTOCOL),
				new CocParserFactory<ResetLoraDacService>(ResetLoraDacService.class));
		chatSystem.registerTranslator(Reconfigure.class, new CocTranslatorFactory<Reconfigure>(Reconfigure.class));
		
		chatSystem.registerApi(ILoraDacService.class, LoraDacService.class);
		chatSystem.registerApi(ILoraDacClient.class, LoraDacClient.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(ILoraDacClient.class);
		chatSystem.unregisterApi(ILoraDacService.class);
		
		chatSystem.unregisterTranslator(Reconfigure.class);
		chatSystem.unregisterParser(new IqProtocolChain(ResetLoraDacService.PROTOCOL));
	}

}
