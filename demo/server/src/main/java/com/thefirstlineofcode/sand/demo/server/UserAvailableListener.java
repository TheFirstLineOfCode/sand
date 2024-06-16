package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.im.ResourceAvailableEvent;
import com.thefirstlineofcode.sand.demo.protocols.NodeConfirmationRequest;
import com.thefirstlineofcode.sand.demo.protocols.NotAuthorizedEdgeThingRegistration;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class UserAvailableListener implements IEventListener<ResourceAvailableEvent> {
	@Dependency("not.authorized.edge.thing.registrations")
	private NotAuthorizedEdgeThingRegistrations notAuthorizedEdgeThingRegistrations;
	
	@Dependency("node.confirmation.requests")
	private NodeConfirmationRequests nodeConfirmationRequests;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IAclService aclService;
	
	@Override
	public void process(IEventContext context, ResourceAvailableEvent event) {
		notifyNotAuthorizedEdgeThingRegistrationsIfUserIsTestUser(context, event);
		notifyNodeConfirmationRequestsIfUserIsOwner(context, event.getJid());
	}

	private void notifyNotAuthorizedEdgeThingRegistrationsIfUserIsTestUser(IEventContext context,
			ResourceAvailableEvent event) {
		for (String user : SandDemoCommandsProcessor.TEST_USERS) {	
			if (user.equals(event.getJid().getNode())) {
				notifyNotAuthorizedEdgeThingRegistrationsToUser(context, event.getJid());
				return;
			}
		}
	}
	
	private void notifyNotAuthorizedEdgeThingRegistrationsToUser(IEventContext context, JabberId user) {
		while (!notAuthorizedEdgeThingRegistrations.isEmpty()) {
			String thingId = notAuthorizedEdgeThingRegistrations.poll();
			context.write(user, new Iq(Iq.Type.SET, new NotAuthorizedEdgeThingRegistration(thingId)));
		}
	}
	
	private void notifyNodeConfirmationRequestsIfUserIsOwner(IEventContext context, JabberId user) {
		while (!nodeConfirmationRequests.isEmpty()) {
			NodeConfirmationRequest nodeConfirmationRequest = nodeConfirmationRequests.peek();
			
			String concentratorThingId = thingManager.getEdgeThingManager().getThingIdByThingName(
					nodeConfirmationRequest.getConcentratorThingName());
			String owner = aclService.getOwner(concentratorThingId);
			
			if (!user.getNode().equals(owner))
				continue;
			
			if (nodeConfirmationRequest != nodeConfirmationRequests.poll()) {
				throw new RuntimeException("Can't poll the same node confirmation request you peeked eariler.");
			}
			
			context.write(user,
					new Iq(Iq.Type.SET, new NodeConfirmationRequest(nodeConfirmationRequest.getConcentratorThingName(),
							nodeConfirmationRequest.getNodeThingId(),
							nodeConfirmationRequest.getCommunicationNet(),
							nodeConfirmationRequest.getRequestedTime())));
		}
	}

}
