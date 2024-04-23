package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.sand.demo.protocols.NotAuthorizedEdgeThingRegistration;
import com.thefirstlineofcode.sand.server.ibtr.NotAuthorizedEdgeThingRegistrationEvent;

public class NotAuthorizedThingRegistrationListener implements IEventListener<NotAuthorizedEdgeThingRegistrationEvent>,
			IServerConfigurationAware {
	@BeanDependency
	private IResourcesService resourcesService;
	
	private String domainName;
	
	@Override
	public void process(IEventContext context, NotAuthorizedEdgeThingRegistrationEvent event) {
		for (String user : SandDemoCommandsProcessor.TEST_USERS) {
			JabberId bareJid = new JabberId(user, domainName);
			IResource[] resources = resourcesService.getResources(bareJid);
			
			if (resources == null || resources.length == 0)
				continue;
			
			for (IResource resource : resources) {
				context.write(resource.getJid(),
						new Iq(Iq.Type.SET, new NotAuthorizedEdgeThingRegistration(event.getThingId())));
			}
		}
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}

}
