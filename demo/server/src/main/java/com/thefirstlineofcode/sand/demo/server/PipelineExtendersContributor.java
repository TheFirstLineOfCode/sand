package com.thefirstlineofcode.sand.demo.server;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.im.ResourceAvailableEvent;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedEdgeThings;
import com.thefirstlineofcode.sand.demo.protocols.DeliverTemperatureToOwner;
import com.thefirstlineofcode.sand.demo.protocols.EdgeThingRegistration;
import com.thefirstlineofcode.sand.demo.protocols.NodeAddition;
import com.thefirstlineofcode.sand.demo.protocols.NodeConfirmationRequest;
import com.thefirstlineofcode.sand.demo.protocols.NotAuthorizedEdgeThingRegistration;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideos;
import com.thefirstlineofcode.sand.demo.protocols.RemoveVideo;
import com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter.CelsiusDegree;
import com.thefirstlineofcode.sand.server.concentrator.NodeAddedEvent;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmationRequestEvent;
import com.thefirstlineofcode.sand.server.ibtr.EdgeThingRegistrationEvent;
import com.thefirstlineofcode.sand.server.ibtr.NotAuthorizedEdgeThingRegistrationEvent;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {

	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.registerCocTranslator(NotAuthorizedEdgeThingRegistration.class);
		configurator.registerCocTranslator(EdgeThingRegistration.class);
		configurator.registerCocTranslator(NodeConfirmationRequest.class);
		configurator.registerCocTranslator(NodeAddition.class);
		
		configurator.registerCocParser(new IqProtocolChain(AccessControlList.PROTOCOL),
				AccessControlList.class);
		configurator.registerCocTranslator(AccessControlList.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(AccessControlList.PROTOCOL),
				new AclProcessor());
		
		configurator.registerCocParser(new IqProtocolChain(AuthorizedEdgeThings.PROTOCOL),
				AuthorizedEdgeThings.class);
		configurator.registerCocTranslator(AuthorizedEdgeThings.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(AuthorizedEdgeThings.PROTOCOL),
				new AuthorizedEdgeThingsProcessor());
		
		configurator.registerEventListener(NotAuthorizedEdgeThingRegistrationEvent.class, new NotAuthorizedThingRegistrationListener());
		configurator.registerEventListener(EdgeThingRegistrationEvent.class, new EdgeThingRegistrationListener());
		configurator.registerEventListener(NodeConfirmationRequestEvent.class, new NodeConfirmationRequestListener());
		configurator.registerEventListener(NodeAddedEvent.class, new NodeAdditionListener());
		configurator.registerEventListener(ResourceAvailableEvent.class, new EdgeThingAvailableEventListener());
		
		configurator.registerPipelinePreprocessor(new AclPipelinePreprocessor());
		
		configurator.registerCocParser(new IqProtocolChain(RecordedVideos.PROTOCOL),
				RecordedVideos.class);
		configurator.registerCocTranslator(RecordedVideos.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(RecordedVideos.PROTOCOL),
				new RecordedVideosProcessor());
		
		configurator.registerCocParser(new IqProtocolChain(DeliverTemperatureToOwner.PROTOCOL), DeliverTemperatureToOwner.class);
		configurator.registerCocTranslator(CelsiusDegree.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(DeliverTemperatureToOwner.PROTOCOL), new DeliverTemperatureToOwnerProcessor());
		
		configurator.registerCocParser(new IqProtocolChain(RemoveVideo.PROTOCOL), RemoveVideo.class);
		configurator.registerSingletonXepProcessor(new IqProtocolChain(RemoveVideo.PROTOCOL), new RemoveVideoProcessor());
	}
	
}
