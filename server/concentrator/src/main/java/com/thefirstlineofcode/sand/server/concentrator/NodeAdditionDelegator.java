package com.thefirstlineofcode.sand.server.concentrator;

import java.util.Calendar;
import java.util.Date;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirer;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirerAware;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@AppComponent("node.addition.delegator")
public class NodeAdditionDelegator implements IEventFirerAware, IConfigurationAware {
	private static final String CONFIGURATION_KEY_NODE_CONFIRMATION_VALIDITY_TIME = "node.confirmation.validity.time";	
	private static final int DEFAULT_NODE_CONFIRMATION_VALIDITY_TIME = 60 * 5;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IAccountManager accountManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	private IEventFirer eventFirer;
	
	private int nodeConfirmationValidityTime;
	
	public void requestToConfirm(NodeConfirmation confirmation) {
		String concentratorThingName = confirmation.getConcentratorThingName();
		
		String concentratorThingId = thingManager.getThingIdByThingName(concentratorThingName);
		if (concentratorThingId == null)
			throw new ProtocolException(new ItemNotFound(String.format("Thing which's thing name is '%s' not be found.",
					concentratorThingName)));
		
		if (!thingManager.isConcentrator(thingManager.getModel(concentratorThingId)))
			throw new ProtocolException(new NotAcceptable("Thing which's thing name is '%s' isn't a concentrator.",
					concentratorThingName));
		
		IConcentrator concentrator = concentratorFactory.getConcentrator(concentratorThingId);
		if (concentrator == null)
			throw new RuntimeException("Can't get the concentrator.");
		
		confirmation.setExpiredTime(getExpiredTime(confirmation.getRequestedTime().getTime(),
				nodeConfirmationValidityTime));
		concentrator.requestToConfirm(confirmation);
		
		eventFirer.fire(new NodeConfirmationRequestEvent(
				concentratorThingName, confirmation.getNode().getThingId(),
				confirmation.getNode().getCommunicationNet(), confirmation.getRequestedTime()));
	}
	
	private Date getExpiredTime(long currentTime, int validityTime) {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.setTimeInMillis(currentTime + (validityTime * 1000));
		
		return expiredTime.getTime();
	}
	
	public NodeAdded confirm(String concentratorThingName, String nodeThingId) {
		String thingId = thingManager.getThingIdByThingName(concentratorThingName);
		if (thingId == null)
			throw new ProtocolException(new ItemNotFound(String.format("Thing which's thing name is '%s' not be found.",
					concentratorThingName)));
		
		if (!thingManager.isConcentrator(thingManager.getModel(thingId)))
			throw new ProtocolException(new NotAcceptable("Thing which's thing name is '%s' isn't a concentrator.",
					concentratorThingName));
		
		IConcentrator concentrator = concentratorFactory.getConcentrator(thingId);
		if (concentrator == null)
			throw new RuntimeException("Can't get the concentrator.");
		
		NodeAdded nodeAdded = concentrator.confirm(nodeThingId);
		eventFirer.fire(createNodeAdditionEvent(nodeAdded));
		
		return nodeAdded;
	}
	
	private NodeAdditionEvent createNodeAdditionEvent(NodeAdded nodeAdded) {
		return new NodeAdditionEvent(nodeAdded.getRequestId(), nodeAdded.getConcentratorThingName(),
				nodeAdded.getNodeThingId(), nodeAdded.getLanId(), nodeAdded.getModel(),
				nodeAdded.getAddedTime());
	}
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		nodeConfirmationValidityTime = configuration.getInteger(CONFIGURATION_KEY_NODE_CONFIRMATION_VALIDITY_TIME,
				DEFAULT_NODE_CONFIRMATION_VALIDITY_TIME);
	}

	@Override
	public void setEventFirer(IEventFirer eventFirer) {
		this.eventFirer = eventFirer;
	}
}
