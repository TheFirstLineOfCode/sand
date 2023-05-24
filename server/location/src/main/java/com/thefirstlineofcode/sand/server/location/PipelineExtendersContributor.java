package com.thefirstlineofcode.sand.server.location;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.sand.protocols.location.LocateThings;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	private static final ProtocolChain IQ_PROTOCOL_CHAIN_LOCATE_THINGS = new IqProtocolChain(LocateThings.PROTOCOL);
	
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.registerCocParser(IQ_PROTOCOL_CHAIN_LOCATE_THINGS, LocateThings.class);
		configurator.registerCocTranslator(LocateThings.class);
		configurator.registerSingletonXepProcessor(IQ_PROTOCOL_CHAIN_LOCATE_THINGS, new LocationProcessor());
	}

}
