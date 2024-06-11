package com.thefirstlineofcode.sand.client.concentrator;

import java.util.Properties;

import com.thefirstlinelinecode.sand.protocols.concentrator.AddNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlinelinecode.sand.protocols.concentrator.PullNodes;
import com.thefirstlinelinecode.sand.protocols.concentrator.RemoveNode;
import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;

public class ConcentratorPlugin implements IPlugin {
	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerTranslator(
				AddNode.class,
				new CocTranslatorFactory<>(AddNode.class)
		);
		chatSystem.registerParser(
				new IqProtocolChain(NodeAdded.PROTOCOL),
				new CocParserFactory<>(NodeAdded.class)
		);
		
		chatSystem.registerParser(
				new IqProtocolChain(RemoveNode.PROTOCOL),
				new CocParserFactory<>(RemoveNode.class)
				);
		
		chatSystem.registerTranslator(
				PullNodes.class,
				new CocTranslatorFactory<>(PullNodes.class)
		);
		chatSystem.registerParser(
				new IqProtocolChain(PullNodes.PROTOCOL),
				new CocParserFactory<>(PullNodes.class)
		);
		
		chatSystem.registerApi(IConcentrator.class, Concentrator.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IConcentrator.class);
		
		chatSystem.unregisterParser(new IqProtocolChain(PullNodes.PROTOCOL));
		chatSystem.unregisterTranslator(PullNodes.class);
		
		chatSystem.unregisterParser(new IqProtocolChain(RemoveNode.PROTOCOL));
		
		chatSystem.unregisterParser(new IqProtocolChain(NodeAdded.PROTOCOL));
		chatSystem.unregisterTranslator(AddNode.class);
	}

}
