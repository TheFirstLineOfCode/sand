package com.thefirstlineofcode.sand.demo.server;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedThings;
import com.thefirstlineofcode.sand.demo.protocols.DeliverTemperatureToOwner;
import com.thefirstlineofcode.sand.demo.protocols.NodeAddition;
import com.thefirstlineofcode.sand.demo.protocols.NodeConfirmationRequest;
import com.thefirstlineofcode.sand.demo.protocols.NotAuthorizedThingRegistration;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideos;
import com.thefirstlineofcode.sand.demo.protocols.ThingRegistration;
import com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter.CelsiusDegree;
import com.thefirstlineofcode.sand.server.concentrator.NodeAdditionEvent;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmationRequestEvent;
import com.thefirstlineofcode.sand.server.ibtr.NotAuthorizedThingRegistrationEvent;
import com.thefirstlineofcode.sand.server.ibtr.ThingRegistrationEvent;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {

	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.registerCocTranslator(NotAuthorizedThingRegistration.class);
		configurator.registerCocTranslator(ThingRegistration.class);
		configurator.registerCocTranslator(NodeConfirmationRequest.class);
		configurator.registerCocTranslator(NodeAddition.class);
		
		configurator.registerCocParser(new IqProtocolChain(AccessControlList.PROTOCOL),
				AccessControlList.class);
		configurator.registerCocTranslator(AccessControlList.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(AccessControlList.PROTOCOL),
				new AclProcessor());
		
		configurator.registerCocParser(new IqProtocolChain(AuthorizedThings.PROTOCOL),
				AuthorizedThings.class);
		configurator.registerCocTranslator(AuthorizedThings.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(AuthorizedThings.PROTOCOL),
				new AuthorizedThingsProcessor());
		
		configurator.registerEventListener(NotAuthorizedThingRegistrationEvent.class, new NotAuthorizedThingRegistrationListener());
		configurator.registerEventListener(ThingRegistrationEvent.class, new ThingRegistrationListener());
		configurator.registerEventListener(NodeConfirmationRequestEvent.class, new NodeConfirmationRequestListener());
		configurator.registerEventListener(NodeAdditionEvent.class, new NodeAdditionListener());
		
		configurator.registerPipelinePreprocessor(new AclPipelinePreprocessor());
		
		configurator.registerCocParser(new IqProtocolChain(RecordedVideos.PROTOCOL),
				RecordedVideos.class);
		configurator.registerCocTranslator(RecordedVideos.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(RecordedVideos.PROTOCOL),
				new RecordedVideosProcessor());
		
		configurator.registerCocParser(new IqProtocolChain(DeliverTemperatureToOwner.PROTOCOL), DeliverTemperatureToOwner.class);
		configurator.registerCocTranslator(CelsiusDegree.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(DeliverTemperatureToOwner.PROTOCOL), new DeliverTemperatureToOwnerProcessor());
	}
	
}
