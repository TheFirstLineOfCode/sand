package com.thefirstlineofcode.sand.server.sensor;

import java.util.Map.Entry;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolChain;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.ProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.ProtocolTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.sensor.Report;
import com.thefirstlineofcode.sand.protocols.sensor.oxm.ReportParserFactory;
import com.thefirstlineofcode.sand.protocols.sensor.oxm.ReportTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	private static final ProtocolChain PROTOCOL_CHAIN_REPORT = new IqProtocolChain(Report.PROTOCOL);
	
	@BeanDependency
	private IThingManager thingManager;
	
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {		
		configurator.
			registerParserFactory(new ProtocolParserFactory<>(new IqProtocolChain(Report.PROTOCOL), new ReportParserFactory())).
			registerTranslatorFactory(new ProtocolTranslatorFactory<>(Report.class, new ReportTranslatorFactory())).
			registerSingletonXepProcessor(PROTOCOL_CHAIN_REPORT, new ReportProcessor());
		
		for (String model : thingManager.getModels()) {
			IThingModelDescriptor modelDescriptor = thingManager.getModelDescriptor(model);
			for (Entry<Protocol, Class<?>> entry : modelDescriptor.getSupportedData().entrySet()) {
				configurator.registerCocParser(
						new IqProtocolChain(Report.PROTOCOL).next(entry.getKey()), entry.getValue());
				configurator.registerCocTranslator(entry.getValue());
			}
		}
	}	
}
