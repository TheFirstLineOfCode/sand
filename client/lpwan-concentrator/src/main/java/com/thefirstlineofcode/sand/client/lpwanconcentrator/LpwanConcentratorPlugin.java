package com.thefirstlineofcode.sand.client.lpwanconcentrator;

import java.util.Properties;

import com.thefirstlinelinecode.sand.protocols.concentrator.AddNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlinelinecode.sand.protocols.concentrator.PullNodes;
import com.thefirstlinelinecode.sand.protocols.concentrator.RemoveNode;
import com.thefirstlinelinecode.sand.protocols.lpwan.concentrator.friends.LanFollows;
import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.client.actuator.ActuatorPlugin;
import com.thefirstlineofcode.sand.client.concentrator.Concentrator;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.friends.FriendsPlugin;
import com.thefirstlineofcode.sand.client.sensor.SensorPlugin;

public class LpwanConcentratorPlugin implements IPlugin {
	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(ActuatorPlugin.class);
		chatSystem.register(FriendsPlugin.class);
		chatSystem.register(SensorPlugin.class);
		
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
		
		chatSystem.registerTranslator(
				LanFollows.class,
				new CocTranslatorFactory<>(LanFollows.class)
				);
		chatSystem.registerParser(
				new IqProtocolChain(LanFollows.PROTOCOL),
				new CocParserFactory<>(LanFollows.class)
				);
		
		chatSystem.registerApi(IConcentrator.class, Concentrator.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IConcentrator.class);
		
		chatSystem.unregisterParser(new IqProtocolChain(LanFollows.PROTOCOL));
		chatSystem.unregisterTranslator(LanFollows.class);
		
		chatSystem.unregisterParser(new IqProtocolChain(PullNodes.PROTOCOL));
		chatSystem.unregisterTranslator(PullNodes.class);
		
		chatSystem.unregisterParser(new IqProtocolChain(RemoveNode.PROTOCOL));
		
		chatSystem.unregisterParser(new IqProtocolChain(NodeAdded.PROTOCOL));
		chatSystem.unregisterTranslator(AddNode.class);
		
		chatSystem.unregister(SensorPlugin.class);
		chatSystem.unregister(FriendsPlugin.class);
		chatSystem.unregister(ActuatorPlugin.class);
	}

}
