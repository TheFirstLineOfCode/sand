package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.im.ResourceAvailableEvent;
import com.thefirstlineofcode.sand.demo.protocols.NotAuthorizedEdgeThingRegistration;

public class UserAvailableListener implements IEventListener<ResourceAvailableEvent>, IServerConfigurationAware {
	@Dependency("not.authorized.edge.thing.registrations")
	private NotAuthorizedEdgeThingRegistrations notAuthorizedEdgeThingRegistrations;
	
	private String domainName;

	@Override
	public void process(IEventContext context, ResourceAvailableEvent event) {
		for (String user : SandDemoCommandsProcessor.TEST_USERS) {
			JabberId bareJid = new JabberId(user, domainName);
			
			if (bareJid.equals(event.getJid().getBareId())) {
				notifyNotAuthorizedEdgeThingRegistrationsToUser(context, event.getJid());
			}
		}
	}
	
	private void notifyNotAuthorizedEdgeThingRegistrationsToUser(IEventContext context, JabberId user) {
		while (!notAuthorizedEdgeThingRegistrations.isEmpty()) {
			String thingId = notAuthorizedEdgeThingRegistrations.poll();
			context.write(user, new Iq(Iq.Type.SET, new NotAuthorizedEdgeThingRegistration(thingId)));
		}
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}

}
