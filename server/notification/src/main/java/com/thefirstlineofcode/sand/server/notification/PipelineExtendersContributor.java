package com.thefirstlineofcode.sand.server.notification;

import java.util.Map.Entry;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolChain;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.ProtocolParserFactory;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.oxm.NotificationParserFactory;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	private static final ProtocolChain PROTOCOL_CHAIN_NOTIFICATION = new IqProtocolChain(Notification.PROTOCOL);
	
	@BeanDependency
	private IThingManager thingManager;
	
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {		
		configurator.
			registerParserFactory(new ProtocolParserFactory<>(new IqProtocolChain(Notification.PROTOCOL), new NotificationParserFactory())).
			registerSingletonXepProcessor(PROTOCOL_CHAIN_NOTIFICATION, new NotificationProcessor());
		
		for (String model : thingManager.getModels()) {
			IThingModelDescriptor modelDescriptor = thingManager.getModelDescriptor(model);
			for (Entry<Protocol, Class<?>> entry : modelDescriptor.getSupportedEvents().entrySet()) {
				configurator.registerCocParser(
						new IqProtocolChain(Notification.PROTOCOL).next(entry.getKey()), entry.getValue());
				configurator.registerCocTranslator(entry.getValue());
			}
		}
	}
}
