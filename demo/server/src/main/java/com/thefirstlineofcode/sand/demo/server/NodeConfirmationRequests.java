package com.thefirstlineofcode.sand.demo.server;

import java.util.concurrent.ArrayBlockingQueue;

import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;
import com.thefirstlineofcode.sand.demo.protocols.NodeConfirmationRequest;

@AppComponent("node.confirmation.requests")
public class NodeConfirmationRequests {
	private ArrayBlockingQueue<NodeConfirmationRequest> nodeConfirmationRequests;
	
	public NodeConfirmationRequests() {
		nodeConfirmationRequests = new ArrayBlockingQueue<>(5, true);
	}
	
	public void put(NodeConfirmationRequest nodeConfirmationRequest) {
		try {			
			nodeConfirmationRequests.put(nodeConfirmationRequest);
		} catch (Exception e) {
			throw new RuntimeException("Can't put node configuration request into queue.", e);
		}
	}
	
	public boolean isEmpty() {
		return nodeConfirmationRequests.isEmpty();
	}
	
	public NodeConfirmationRequest peek() {
		return nodeConfirmationRequests.peek();
	}
	
	public NodeConfirmationRequest poll() {
		return nodeConfirmationRequests.poll();
	}
}
