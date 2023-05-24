package com.thefirstlineofcode.sand.demo.client;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.demo.protocols.NodeAddition;
import com.thefirstlineofcode.sand.demo.protocols.NodeConfirmationRequest;
import com.thefirstlineofcode.sand.demo.protocols.NotAuthorizedThingRegistration;
import com.thefirstlineofcode.sand.demo.protocols.ThingRegistration;

public class NetConfigService implements INetConfigService, IIqListener {
	private IChatServices chatServices;
	
	private NetConfigEventsListener listener;

	@Override
	public void startToListenNetConfigEvents(NetConfigEventsListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Null listener.");
		
		this.listener = listener;
		
		chatServices.getIqService().addListener(NotAuthorizedThingRegistration.PROTOCOL, this);
		chatServices.getIqService().addListener(ThingRegistration.PROTOCOL, this);
		chatServices.getIqService().addListener(NodeConfirmationRequest.PROTOCOL, this);
		chatServices.getIqService().addListener(NodeAddition.PROTOCOL, this);
	}
	
	@Override
	public void stopToListenNetConfigEvents() {
		chatServices.getIqService().removeListener(NodeAddition.PROTOCOL);
		chatServices.getIqService().removeListener(NodeConfirmationRequest.PROTOCOL);
		chatServices.getIqService().removeListener(ThingRegistration.PROTOCOL);
		chatServices.getIqService().removeListener(NotAuthorizedThingRegistration.PROTOCOL);
		listener = null;
	}

	@Override
	public void received(Iq iq) {
		if (iq.getObject() instanceof NotAuthorizedThingRegistration) {
			NotAuthorizedThingRegistration thingRegistration = iq.getObject();
			NetConfigService.this.listener.tryToRegisterWithoutAuthoration(thingRegistration.getThingId());			
		} else if (iq.getObject() instanceof ThingRegistration) {
			ThingRegistration thingRegistration = iq.getObject();;
			NetConfigService.this.listener.thingRegistered(thingRegistration.getThingId(), thingRegistration.getThingName(),
					thingRegistration.getAuthorizer(), thingRegistration.getRegistrationTime());
		} else if (iq.getObject() instanceof NodeConfirmationRequest) {
			NodeConfirmationRequest nodeConfirmationRequest = iq.getObject();;
			NetConfigService.this.listener.requestToConfirm(nodeConfirmationRequest.getConcentratorThingName(),
					nodeConfirmationRequest.getNodeThingId(), nodeConfirmationRequest.getRequestedTime());
		} else if (iq.getObject() instanceof NodeAddition) {
			NodeAddition nodeAddition = iq.getObject();
			NetConfigService.this.listener.nodeAdded(nodeAddition.getConcentratorThingName(),
					nodeAddition.getNodeThingId(), nodeAddition.getLanId(), nodeAddition.getAdditionTime());
		} else {
			
		} 
		
	}

}
