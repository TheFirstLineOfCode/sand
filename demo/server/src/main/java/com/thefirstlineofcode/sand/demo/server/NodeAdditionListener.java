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
import com.thefirstlineofcode.sand.demo.protocols.NodeAddition;
import com.thefirstlineofcode.sand.server.concentrator.NodeAddedEvent;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class NodeAdditionListener implements IEventListener<NodeAddedEvent>, IServerConfigurationAware {
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IAclService aclService;
	
	@BeanDependency
	private IResourcesService resourcesService;
	
	private String domainName;
	
	@Override
	public void process(IEventContext context, NodeAddedEvent event) {
		String concentratorThingId = thingManager.getEdgeThingManager().getThingIdByThingName(event.getNodeAdded().getConcentratorThingName());
		String owner = aclService.getOwner(concentratorThingId);
		
		JabberId bareJidOwner = new JabberId(owner, domainName);
		IResource[] resources = resourcesService.getResources(bareJidOwner);
		
		if (resources == null || resources.length == 0)
			return;
		
		for (IResource resource : resources) {
			context.write(resource.getJid(),
					new Iq(Iq.Type.SET, new NodeAddition(event.getNodeAdded().getConcentratorThingName(),
							event.getNodeAdded().getNodeThingId(), event.getNodeAdded().getLanId(),
							event.getNodeAdded().getAdditionTime())));
		}
	}
	
	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}
}
