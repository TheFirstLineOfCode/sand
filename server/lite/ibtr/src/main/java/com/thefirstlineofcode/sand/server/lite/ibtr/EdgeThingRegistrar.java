package com.thefirstlineofcode.sand.server.lite.ibtr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAuthorized;
import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirer;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirerAware;
import com.thefirstlineofcode.sand.server.ibtr.IEdgeThingRegistrar;
import com.thefirstlineofcode.sand.server.ibtr.NotAuthorizedThingRegistrationEvent;
import com.thefirstlineofcode.sand.server.ibtr.EdgeThingRegistrationEvent;
import com.thefirstlineofcode.sand.server.things.EdgeThingRegistered;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@AppComponent("edge.thing.registrar")
public class EdgeThingRegistrar implements IEdgeThingRegistrar, IEventFirerAware {
	private static final Logger logger = LoggerFactory.getLogger(EdgeThingRegistrar.class);
	
	@BeanDependency
	private IThingManager thingManager;
	
	private IEventFirer eventFirer;
	
	@Override
	public EdgeThingRegistered register(String thingId, String registrationCode) {
		try {
			EdgeThingRegistered registered = thingManager.getEdgeThingManager().register(thingId, registrationCode);
			if (logger.isInfoEnabled())
				logger.info("Edge thing which's thing ID is '{}' has registered. It's thing name is assigned to '{}'.",
						thingId, registered.registeredEdgeThing.getThingName());
			
			eventFirer.fire(new EdgeThingRegistrationEvent(registered.thingId, registered.registeredEdgeThing.getThingName(),
						registered.authorizer, registered.registrationTime));
			
			return registered;
		} catch (ProtocolException e) {
			if (e.getError() instanceof NotAuthorized) {
				if (logger.isWarnEnabled())
					logger.warn("Edge thing which's thing ID is '{}' tried to register without authorization.", thingId);
				
				eventFirer.fire(new NotAuthorizedThingRegistrationEvent(thingId));
			}
			
			throw e;
		}
	}

	@Override
	public void remove(String thingId) {
		// TODO
	}

	@Override
	public void setEventFirer(IEventFirer eventFirer) {
		this.eventFirer = eventFirer;
	}
	
}
