package com.thefirstlineofcode.sand.server.p2p.webcam;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.sand.protocols.webrtc.signaling.Signal;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	private static final ProtocolChain IQ_PROTOCOL_CHAIN_SIGNAL = new IqProtocolChain(Signal.PROTOCOL);
	
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.registerCocParser(IQ_PROTOCOL_CHAIN_SIGNAL, Signal.class);
		configurator.registerCocTranslator(Signal.class);
		configurator.registerSingletonXepProcessor(IQ_PROTOCOL_CHAIN_SIGNAL, new SignalProcessor());
	}

}
