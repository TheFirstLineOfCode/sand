package com.thefirstlineofcode.sand.server.concentrator;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;

public interface IConcentrator {
	public static final int LAN_ID_CONCENTRATOR = 0;
	
	boolean containsNode(String nodeThingId);
	boolean containsLanId(int lanId);
	void requestToConfirm(NodeConfirmation confirmation);
	void cancelConfirmation(String nodeThingId);
	NodeConfirmed confirm(String nodeThingId);
	Node getNodeByLanId(int lanId);
	Node getNodeByThingId(String nodeThingId);
	Node[] getNodes();
	NodeAdded addNode(Node node);
	void removeNode(int lanId);
}
