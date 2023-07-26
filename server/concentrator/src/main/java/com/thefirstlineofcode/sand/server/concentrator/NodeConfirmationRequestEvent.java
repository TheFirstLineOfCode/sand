package com.thefirstlineofcode.sand.server.concentrator;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;

public class NodeConfirmationRequestEvent implements IEvent {
	private NodeConfirmation confirmation;
	
	public NodeConfirmationRequestEvent(NodeConfirmation confirmation) {
		this.confirmation = confirmation;
	}
	
	public NodeConfirmation getConfirmation() {
		return confirmation;
	}
	
	public Object clone() {
		return new NodeConfirmationRequestEvent(confirmation);
	}
}
