package com.thefirstlineofcode.sand.client.concentrator;

import java.util.Collection;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.sand.client.actuator.IActuator;
import com.thefirstlineofcode.sand.client.friends.IFollowService;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.sensor.Report.QoS;
import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;
import com.thefirstlineofcode.sand.protocols.thing.IAddress;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;

public interface IConcentrator extends IActuator, IFollowService {
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
	
	void addCommunicator(CommunicationNet net, ICommunicator<?, ?, byte[]> communicator);
	ICommunicator<?, ?, byte[]> getCommunicator(CommunicationNet communicationNet);
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
	void pullLanFollows();
	void pullLanFollows(PullLanFollowsListener pullLanFollowsListener);
	String getThingName();
	void registerLanThingModel(IThingModelDescriptor modelDescriptor);
	void registerLanExecutionErrorConverter(ILanExecutionErrorConverter lanActionErrorConverter);
	void setDefaultLanExecutionTimeout(long timeout);
	long getDefaultLanExecutionTimeout();
	void setLanExecutionTimeoutCheckInterval(int interval);
	int getLanExecutionTimeoutCheckInterval();
	void addListener(IConcentrator.Listener listener);
	IConcentrator.Listener removeListener(IConcentrator.Listener listener);
	boolean isLanActionSupported(String model, Protocol protocol);
	boolean isLanActionSupported(String model, Class<?> actionType);
	Class<?> getLanActionType(String model, Protocol protocol);
	boolean isLanEventSupported(String model, Protocol protocol);
	boolean isLanEventSupported(String model, Class<?> eventType);
	boolean isLanEventFollowed(String model, Protocol protocol);
	boolean isLanEventFollowed(String model, Class<?> eventType);
	Class<?> getLanEventType(String model, Protocol protocol);
	boolean isLanDataSupported(String model, Protocol protocol);
	boolean isLanDataSupported(String model, Class<?> dataType);
	Class<?> getLanDataType(String model, Protocol protocol);
	void enableLanRouting();
	void disableLanRouting();
	boolean isLanRoutingEnabled();
	boolean isConfigured(String thingId);
	void setAckRequiredLanNotificationsPoolSize(int size);
	int getAckRequiredLanNotificationsPoolSize();
	void addLanNotificationPreprocessor(ILanNotificationPreprocessor lanNotificationPreprocessor);
	boolean removeLanNotificationPreprocessor(ILanNotificationPreprocessor lanNotificationPreprocessor);
	void addLanReportPreprocessor(ILanReportPreprocessor lanReportPreprocessor);
	boolean removeLanReportPreprocessor(ILanReportPreprocessor lanReportPreprocessor);
	void setNotificationRexInitInternal(long rexInitInternal);
	long getNotificationRexInitInternal();
	void setNotificationRexTimeout(long rexTimeout);
	long getNotificationRexTimeout();
	void setDefaultDataQoS(QoS qos);
	QoS getDefaultDataQoS();
	void setDataQoS(Class<?> dataType, QoS qos);
	QoS getDataQoS(Class<?> dataType);
	
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
	
	public interface PullLanFollowsListener {
		void lanFollowsPulled();
		void occurred(StanzaError error);
	}
}
