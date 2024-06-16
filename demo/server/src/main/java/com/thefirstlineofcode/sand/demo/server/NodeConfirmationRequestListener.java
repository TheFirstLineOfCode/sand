package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.sand.demo.protocols.NodeConfirmationRequest;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmationRequestEvent;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class NodeConfirmationRequestListener implements IEventListener<NodeConfirmationRequestEvent>,
			IServerConfigurationAware  {
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IAclService aclService;
	
	@BeanDependency
	private IResourcesService resourcesService;
	
	@Dependency("node.confirmation.requests")
	private NodeConfirmationRequests nodeConfirmationRequests;
	
	private String domainName;
	
	@Override
	public void process(IEventContext context, NodeConfirmationRequestEvent event) {
		if (!notifyOnlineOwners(context, event)) {			
			nodeConfirmationRequests.put(new NodeConfirmationRequest(event.getConfirmation().getConcentratorThingName(),
					event.getConfirmation().getNode().getThingId(),
					event.getConfirmation().getNode().getCommunicationNet(),
					event.getConfirmation().getRequestedTime()));
		}
		
		
	}
	
	private boolean notifyOnlineOwners(IEventContext context, NodeConfirmationRequestEvent event) {
		String concentratorThingId = thingManager.getEdgeThingManager().getThingIdByThingName(event.getConfirmation().getConcentratorThingName());
		String owner = aclService.getOwner(concentratorThingId);
		
		JabberId bareJidOwner = new JabberId(owner, domainName);
		IResource[] resources = resourcesService.getResources(bareJidOwner);
		
		if (resources == null || resources.length == 0)
			return false;
		
		NodeConfirmationRequest nodeConfirmationRequest = new NodeConfirmationRequest(event.getConfirmation().getConcentratorThingName(),
				event.getConfirmation().getNode().getThingId(),
				event.getConfirmation().getNode().getCommunicationNet(),
				event.getConfirmation().getRequestedTime());
				
		for (IResource resource : resources) {
			context.write(resource.getJid(),new Iq(Iq.Type.SET, nodeConfirmationRequest));
		}
		
		return true;
	}
	
	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}
}
