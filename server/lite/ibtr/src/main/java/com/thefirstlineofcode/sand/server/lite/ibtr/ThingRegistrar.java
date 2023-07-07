package com.thefirstlineofcode.sand.server.lite.ibtr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAuthorized;
import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirer;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirerAware;
import com.thefirstlineofcode.sand.server.ibtr.IThingRegistrar;
import com.thefirstlineofcode.sand.server.ibtr.NotAuthorizedThingRegistrationEvent;
import com.thefirstlineofcode.sand.server.ibtr.ThingRegistrationEvent;
import com.thefirstlineofcode.sand.server.things.IThingManager;
import com.thefirstlineofcode.sand.server.things.ThingRegistered;

@AppComponent("thing.registrar")
public class ThingRegistrar implements IThingRegistrar, IEventFirerAware {
	private static final Logger logger = LoggerFactory.getLogger(ThingRegistrar.class);
	
	@BeanDependency
	private IThingManager thingManager;
	
	private IEventFirer eventFirer;
	
	@Override
	public ThingRegistered register(String thingId, String registrationCode) {
		try {
			ThingRegistered registered = thingManager.register(thingId, registrationCode);
			if (logger.isInfoEnabled())
				logger.info("Thing which's thing ID is '{}' has registered. It's thing name is assigned to '{}'.",
						thingId, registered.registeredThing.getThingName());
			
			eventFirer.fire(new ThingRegistrationEvent(registered.thingId, registered.registeredThing.getThingName(),
						registered.authorizer, registered.registrationTime));
			
			return registered;
		} catch (ProtocolException e) {
			if (e.getError() instanceof NotAuthorized) {
				if (logger.isWarnEnabled())
					logger.warn("Thing which's thing ID is '{}' tried to register without authorization.", thingId);
				
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
