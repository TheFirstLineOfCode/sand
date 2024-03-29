package com.thefirstlineofcode.sand.server.concentrator;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.thefirstlinelinecode.sand.protocols.concentrator.AddNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirer;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirerAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class AddNodeProcessor implements IXepProcessor<Iq, AddNode>, IEventFirerAware {
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@Dependency("node.confirmation.delegator")
	private NodeConfirmationDelegator nodeConfirmationDelegator;
	
	private IEventFirer eventFirer;
	
	@Override
	public void process(IProcessingContext context, Iq iq, AddNode xep) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		String thingId = thingManager.getEdgeThingManager().getThingIdByThingName(context.getJid().getNode());
		if (thingId == null)
			throw new ProtocolException(new ItemNotFound(String.format("Edge thing which's thing name is '%s' not be found.",
					context.getJid().getNode())));
		
		if (!thingManager.isConcentrator(thingManager.getModel(thingId)))
			throw new ProtocolException(new ServiceUnavailable("Thing which's thing name is '%s' isn't a concentrator.",
					context.getJid().getNode()));
		
		IConcentrator concentrator = concentratorFactory.getConcentrator(thingId);
		if (concentrator == null)
			throw new RuntimeException("Can't get the concentrator.");
		
		if (concentrator.containsNode(xep.getThingId())) {
			throw new ProtocolException(new Conflict(String.format("Reduplicate node which's ID is '%s'.", xep.getThingId())));
		}
		
		if (xep.getLanId() <= 0 || xep.getLanId() > 255)
			throw new ProtocolException(new BadRequest(String.format("Invlid LAN ID: %d.", xep.getLanId())));			
		
		if (concentrator.containsLanId(xep.getLanId())) {
			throw new ProtocolException(new Conflict(String.format("Reduplicate LAN ID: '%s'.", xep.getLanId())));
		}
		
		if (!thingManager.isUnregisteredThing(xep.getThingId(), xep.getRegistrationCode())) {
			throw new ProtocolException(new NotAcceptable());
		}
				
		Node node = new Node();
		node.setThingId(xep.getThingId());
		node.setRegistrationCode(xep.getRegistrationCode());
		node.setLanId(xep.getLanId());
		node.setCommunicationNet(xep.getCommunicationNet());
		node.setAddress(xep.getAddress());
		node.setModel(thingManager.getModel(xep.getThingId()));
		
		if (thingManager.isConfirmationRequired()) {
			requestToConfirm(context, iq, thingManager.getEdgeThingManager().getThingNameByThingId(thingId), node);
		} else {
			addNode(context, iq, thingId, node);
		}
	}

	private void addNode(IProcessingContext context, Iq iq, String concentratorThingId, Node node) {
		IConcentrator concentrator = concentratorFactory.getConcentrator(concentratorThingId);
		if (concentrator == null) {
			throw new RuntimeException(String.format("No concentrator which's thing ID is '%s'.", concentratorThingId));
		}
		
		NodeAdded nodeAdded = concentrator.addNode(node);
		
		Iq result = Iq.createResult(iq);
		result.setObject(nodeAdded);
		context.write(context.getJid(), result);
		
		eventFirer.fire(new NodeAddedEvent(nodeAdded));
	}

	private void requestToConfirm(IProcessingContext context, Iq iq, String concentratorThingName, Node node) {
		NodeConfirmation confirmation = new NodeConfirmation();
		confirmation.setId(UUID.randomUUID().toString());
		confirmation.setRequestId(iq.getId());
		confirmation.setConcentratorThingName(concentratorThingName);
		confirmation.setNode(node);
		Date currentTime = Calendar.getInstance().getTime();
		confirmation.setRequestedTime(currentTime);
		
		nodeConfirmationDelegator.requestToConfirm(confirmation);
		
		eventFirer.fire(new NodeConfirmationRequestEvent(confirmation));
	}
	
	@Override
	public void setEventFirer(IEventFirer eventFirer) {
		this.eventFirer = eventFirer;
	}
}
