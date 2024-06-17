package com.thefirstlineofcode.sand.client.operator;

import java.util.Properties;

import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.protocols.operator.ApproveFollow;
import com.thefirstlineofcode.sand.protocols.operator.AuthorizeThing;
import com.thefirstlineofcode.sand.protocols.operator.ConfirmConcentration;

public class OperatorPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerTranslator(AuthorizeThing.class,
				new CocTranslatorFactory<AuthorizeThing>(AuthorizeThing.class));
		chatSystem.registerTranslator(ConfirmConcentration.class,
				new CocTranslatorFactory<ConfirmConcentration>(ConfirmConcentration.class));
		chatSystem.registerTranslator(ApproveFollow.class,
				new CocTranslatorFactory<ApproveFollow>(ApproveFollow.class));
		chatSystem.registerParser(new IqProtocolChain(NodeAdded.PROTOCOL),
				new CocParserFactory<NodeAdded>(NodeAdded.class));
		
		chatSystem.registerApi(IOperator.class, Operator.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IOperator.class);
		
		chatSystem.unregisterParser(new IqProtocolChain(NodeAdded.PROTOCOL));
		chatSystem.unregisterTranslator(ApproveFollow.class);
		chatSystem.unregisterTranslator(ConfirmConcentration.class);
		chatSystem.unregisterTranslator(AuthorizeThing.class);
	}

}
