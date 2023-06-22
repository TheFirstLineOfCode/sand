package com.thefirstlineofcode.sand.demo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.im.stanza.Presence;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.granite.framework.im.ResourceAvailableEvent;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class EdgeThingAvailableEventListener implements IEventListener<ResourceAvailableEvent> {
	private static final Logger logger = LoggerFactory.getLogger(EdgeThingAvailableEventListener.class);
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IAclService aclService;
	
	@BeanDependency
	private IResourcesService resourceService;
	
	@Override
	public void process(IEventContext context, ResourceAvailableEvent event) {
		if (!thingManager.thingNameExists(event.getJid().getNode())) {
			if (logger.isDebugEnabled())
				logger.debug("Not a thing. Node name: {}.", event.getJid().getNode());
			return;
		}
		
		String thingId = thingManager.getThingIdByThingName(event.getJid().getNode());
		String owner = aclService.getOwner(thingId);
		
		if (owner == null) {
			if (logger.isErrorEnabled())
				logger.error("Thing owner not found. Thing name: {}.", event.getJid().getNode());
			return;
		}
		
		String domain = event.getJid().getDomain();
		JabberId ownerJid = new JabberId(owner, domain);
		
		IResource[] ownerResources = resourceService.getResources(ownerJid);
		if (ownerResources == null || ownerResources.length == 0)
			return;
		
		Presence presence = new Presence();
		presence.setFrom(event.getJid());
		
		for (IResource resource : ownerResources) {
			context.write(resource.getJid(), presence);
		}
	}

}
