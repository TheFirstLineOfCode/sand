package com.thefirstlineofcode.sand.server.concentrator;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;

public class NodeConfirmedEvent implements IEvent  {
	private NodeConfirmed nodeConfirmed;
	
	public NodeConfirmedEvent (NodeConfirmed nodeConfirmed) {
		this.nodeConfirmed = nodeConfirmed;
	}
	
	public NodeConfirmed getNodeConfirmed() {
		return nodeConfirmed;
	}

	@Override
	public Object clone() {
		return new NodeConfirmedEvent(nodeConfirmed);
	}
}
