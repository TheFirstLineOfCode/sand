package com.thefirstlineofcode.sand.server.concentrator;

import org.pf4j.Extension;

import com.thefirstlinelinecode.sand.protocols.concentrator.AddNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlinelinecode.sand.protocols.concentrator.PullNodes;
import com.thefirstlinelinecode.sand.protocols.concentrator.RemoveNode;
import com.thefirstlinelinecode.sand.protocols.lpwanconcentrator.friends.LanFollows;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.registerCocParser(new IqProtocolChain(AddNode.PROTOCOL), AddNode.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(AddNode.PROTOCOL), new AddNodeProcessor());
		configurator.registerCocTranslator(NodeAdded.class);
		
		configurator.registerCocParser(new IqProtocolChain(PullNodes.PROTOCOL), PullNodes.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(PullNodes.PROTOCOL), new PullNodesProcessor());
		configurator.registerCocTranslator(PullNodes.class);
		
		configurator.registerCocParser(new IqProtocolChain(RemoveNode.PROTOCOL), RemoveNode.class);
		configurator.registerCocTranslator(RemoveNode.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(RemoveNode.PROTOCOL), new RemoveNodeProcessor());
		
		configurator.registerCocParser(new IqProtocolChain(LanFollows.PROTOCOL), LanFollows.class);
		configurator.registerCocTranslator(LanFollows.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(LanFollows.PROTOCOL), new LanFollowsProcessor());
	}
}
