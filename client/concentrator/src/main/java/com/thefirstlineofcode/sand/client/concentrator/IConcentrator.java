package com.thefirstlineofcode.sand.client.concentrator;

import java.util.Collection;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.sand.protocols.thing.IAddress;

public interface IConcentrator {
	public static final int LAN_ID_CONCENTRATOR = 0;
	public static final int MAX_LAN_SIZE = 256;
	
	public enum AddNodeError {
		SIZE_OVERFLOW,
		REDUPLICATE_THING_ID,
		REDUPLICATE_THING_ADDRESS,
		REDUPLICATE_LAN_ID,
		ADDED_NODE_NOT_FOUND,
		SERVER_CHANGED_LAN_ID,
		BAD_NODE_ADDITION_RESPONSE,
		NO_SUCH_CONCENTRATOR,
		NOT_CONCENTRATOR,
		REDUPLICATE_NODE_OR_LAN_ID,
		NOT_UNREGISTERED_THING,
		REMOTE_SERVER_TIMEOUT,
		UNKNOWN_ERROR
	}
	
	int getBestSuitedNewLanId();
	void setAddNodeTimeout(long addNodeTimeout);
	long getAddNodeTimeout();
	void requestServerToAddNode(String thingId, String registrationCode, int lanId, IAddress address);
	void removeNode(int lanId) throws NodeNotFoundException;
	void cleanNodes();
	void setNodes(Map<Integer, LanNode> nodes);
	Collection<LanNode> getNodes();
	LanNode getNode(int lanId);
	void syncNodesWithServer(SyncNodesListener syncNodesListener);
	String getThingName();
	void addListener(IConcentrator.Listener listener);
	IConcentrator.Listener removeListener(IConcentrator.Listener listener);
	
	public interface Listener {
		void nodeAdded(int lanId, LanNode node);
		void nodeReset(int lanId, LanNode node);
		void nodeRemoved(int lanId, LanNode node);
		void occurred(AddNodeError error, LanNode source);
	}
	
	public interface SyncNodesListener {
		void nodesSynced();
		void occurred(StanzaError error);
	}
}
