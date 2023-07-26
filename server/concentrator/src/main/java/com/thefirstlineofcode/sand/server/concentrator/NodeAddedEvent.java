package com.thefirstlineofcode.sand.server.concentrator;

import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;

public class NodeAddedEvent implements IEvent  {
	private NodeAdded nodeAdded;
	
	public NodeAddedEvent (NodeAdded nodeAdded) {
		this.nodeAdded = nodeAdded;
	}
	
	public NodeAdded getNodeAdded() {
		return nodeAdded;
	}

	@Override
	public Object clone() {
		return new NodeAddedEvent(nodeAdded);
	}
}
