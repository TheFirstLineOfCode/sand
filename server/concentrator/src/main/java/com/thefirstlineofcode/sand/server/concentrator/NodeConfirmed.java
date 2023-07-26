package com.thefirstlineofcode.sand.server.concentrator;

import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;

public class NodeConfirmed {
	private String requestId;
	private NodeAdded nodeAdded;
	
	public NodeConfirmed(String requestId, NodeAdded nodeAdded) {
		this.requestId = requestId;
		this.nodeAdded = nodeAdded;
	}
	
	public String getRequestId() {
		return requestId;
	}
	
	public NodeAdded getNodeAdded() {
		return nodeAdded;
	}
}
