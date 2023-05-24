package com.thefirstlineofcode.sand.server.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.RecipientUnavailable;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.location.ILocationService;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class ExecutionProcessor implements IXepProcessor<Iq, Execution> {
	private static final Logger logger = LoggerFactory.getLogger(ExecutionProcessor.class);
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@BeanDependency
	private IResourcesService resourcesService;
	
	@BeanDependency
	private ILocationService locationService;

	@Override
	public void process(IProcessingContext context, Iq iq, Execution xep) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		JabberId target = iq.getTo();
		
		String edgeThingName = target.getNode();
		if (!thingManager.thingNameExists(edgeThingName)) {
			logger.error("Thing which's thing name is '{}' not be found.", edgeThingName);
			throw new ProtocolException(new ItemNotFound(String.format("Thing which's thing name is '%s' not be found."), edgeThingName));
		}
		String lanId = target.getResource();
		
		String thingId = locationService.getThingIdByJid(target);
		if (thingId == null) {
			logger.error("Thing which's JID is '{}' not be found.", target);
			throw new ProtocolException(new ItemNotFound(String.format("Thing which's JID is '%s' not be found.", target)));
		}
		
		String model = thingManager.getModel(thingId);
		if (!thingManager.isActuator(model)) {
			logger.error("Can't do execution. Thing which's model is '{}' isn't an actuator.", model);
			throw new RuntimeException(String.format("Can't do execution. Thing which's model is '%s' isn't an actuator.", model));
		}
		
		if (!thingManager.isActionSupported(model, xep.getAction().getClass())) {
			logger.error("Can't do execution. Action not be supported by thing which's model is '{}'.", model);
			throw new RuntimeException(String.format("Can't do execution. Action not be supported by thing which's model is '%s'.", model));
		}
		
		if (!doesExecutedOnNode(lanId) && (xep.isLanTraceable() || xep.getLanTimeout() != null)) {
			logger.warn("Action will be executed on edge thing which's thing ID is '{}'. Execution parameters 'lan-traceable' and 'lan-timeout'. will be ignored.", thingId);
		}
		
		JabberId edgeTarget = new JabberId(target.getNode(), target.getDomain(), ThingIdentity.DEFAULT_RESOURCE_NAME);
		IResource resource = resourcesService.getResource(edgeTarget);
		if (resource == null) {
			logger.error("Can't deliver execution. Edge thing which's thing name is '{}' wasn't online.", edgeThingName);
			throw new ProtocolException(new RecipientUnavailable(String.format("Can't deliver execution. Edge thing which's thing name is '%s' isn't being online.", edgeThingName)));
		}
		
		context.write(edgeTarget, iq);
	}
	
	private boolean doesExecutedOnNode(String lanId) {
		return lanId != null && !ThingIdentity.DEFAULT_RESOURCE_NAME.equals(lanId);
	}
}
