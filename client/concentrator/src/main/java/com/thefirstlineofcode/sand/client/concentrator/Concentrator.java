package com.thefirstlineofcode.sand.client.concentrator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlinelinecode.sand.protocols.concentrator.AddNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlinelinecode.sand.protocols.concentrator.PullNodes;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.RemoteServerTimeout;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.sand.protocols.thing.BadAddressException;
import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;
import com.thefirstlineofcode.sand.protocols.thing.IAddress;

public class Concentrator implements IConcentrator {
	protected static final Logger logger = LoggerFactory.getLogger(Concentrator.class);
	
	protected static final long DEFAULT_ADD_NODE_TIMEOUT = 1000 * 60 * 5;
	
	protected List<IConcentrator.Listener> listeners;
	
	protected IChatServices chatServices;
	
	protected Map<Integer, LanNode> nodes;
	protected Map<String, LanNode> confirmingNodes;
	protected Object nodesLock;
	protected long addNodeTimeout;
		
	public Concentrator(IChatServices chatServices)  {
		this.chatServices = chatServices;
		
		listeners = new ArrayList<>();
		
		nodes = new LinkedHashMap<>();
		confirmingNodes = new LinkedHashMap<>();
		nodesLock = new Object();
		
		addNodeTimeout = DEFAULT_ADD_NODE_TIMEOUT;
	}
	
	protected JabberId getLanNodeJid(int lanId) {
		JabberId concentratorJid = chatServices.getStream().getJid();
		return new JabberId(concentratorJid.getNode(), concentratorJid.getDomain(), String.valueOf(lanId));
	}
	
	public void addNode(LanNode node) {
		synchronized (nodesLock) {
			this.nodes.put(node.getLanId(), node);
		}
	}

	@Override
	public void requestServerToAddNode(String thingId, String registrationCode, int lanId, IAddress address) {
		synchronized (nodesLock) {
			LanNode node = new LanNode();
			node.setThingId(thingId);
			node.setRegistrationCode(registrationCode);
			node.setLanId(lanId);
			node.setCommunicationNet(address.getCommunicationNet());
			node.setAddress(address.toAddressString());
			node.setConfirmed(false);
			
			if (nodes.size() > (MAX_LAN_SIZE - 1)) {
				if (logger.isErrorEnabled()) {
					logger.error("Nodes size overflow.");
				}
				
				processAddNodeError(AddNodeError.SIZE_OVERFLOW, node);
				
				return;
			}
			
			for (Entry<Integer, LanNode> entry : nodes.entrySet()) {
				if (entry.getValue().getThingId().equals(node.getThingId())) {
					if (logger.isErrorEnabled()) {
						logger.error("Reduplicate thing ID: {}.", node.getThingId());
					}
					
					processAddNodeError(AddNodeError.REDUPLICATE_THING_ID, node);					
					return;
				}
				
				if (entry.getValue().getAddress().equals(node.getAddress())) {
					if (logger.isErrorEnabled()) {
						logger.error("Reduplicate thing address: {}.", node.getAddress());
					}
					
					processAddNodeError(AddNodeError.REDUPLICATE_THING_ADDRESS, node);
					return;
				}
				
				if (entry.getValue().getLanId() == node.getLanId()) {
					if (logger.isErrorEnabled()) {
						logger.error("Reduplicate thing LAN ID: {}.", node.getLanId());
					}
					
					processAddNodeError(AddNodeError.REDUPLICATE_LAN_ID, node);
					return;
				}
			}
			
			chatServices.getTaskService().execute(new AddNodeTask(node));
			
			if (logger.isInfoEnabled()) {
				logger.info("Add node request for node which's thingID is '{}' and address is '{}' has sent.",
						thingId, address);
			}
		}
	}
	
	private class AddNodeTask implements ITask<Iq> {
		private LanNode node;
		
		public AddNodeTask(LanNode node) {
			this.node = node;
		}

		@Override
		public void trigger(IUnidirectionalStream<Iq> stream) {
			AddNode addNode = new AddNode();
			addNode.setThingId(node.getThingId());
			addNode.setRegistrationCode(node.getRegistrationCode());
			addNode.setLanId(node.getLanId());
			addNode.setCommunicationNet(node.getCommunicationNet().toString());
			addNode.setAddress(node.getAddress());
			
			Iq iq = new Iq(Iq.Type.SET, addNode);
			stream.send(iq, addNodeTimeout);
			
			synchronized (nodesLock) {
				confirmingNodes.put(iq.getId(), node);
			}
		}
		
		@Override
		public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
			NodeAdded nodeAdded = iq.getObject();
			
			LanNode confirmingNode = confirmingNodes.get(iq.getId());
			if (confirmingNode == null) {
				if (logger.isErrorEnabled()) {
					logger.error("Confirming node which's thing ID is '{}' not found.", nodeAdded.getNodeThingId());
				}
				
				processAddNodeError(IConcentrator.AddNodeError.ADDED_NODE_NOT_FOUND, node);
				return;
			}
			
			if (!getThingName().equals(nodeAdded.getConcentratorThingName()) ||
					!nodeAdded.getNodeThingId().equals(confirmingNode.getThingId()) ||
					nodeAdded.getLanId() == null ||
					nodeAdded.getModel() == null) {
				if (logger.isErrorEnabled()) {
					logger.error("Bad node addition response. Thing name of concentrator is {}. Thing ID of confirming node is {} and Thing ID of created node is {}. Thing name of concentrator returned by server is {}.",
							getThingName(), confirmingNode.getThingId(), nodeAdded.getNodeThingId(), nodeAdded.getConcentratorThingName());
				}
				
				processAddNodeError(IConcentrator.AddNodeError.BAD_NODE_ADDITION_RESPONSE, node);			
				return;
			}
			
			if (!nodeAdded.getLanId().equals(confirmingNode.getLanId())) {
				if (logger.isErrorEnabled()) {
					logger.error("Bad node addition response. The server changed LAN ID of node to {}.", nodeAdded.getLanId());
				}
				
				processAddNodeError(IConcentrator.AddNodeError.SERVER_CHANGED_LAN_ID, node);
				return;
			}
			
			synchronized (nodesLock) {
				confirmingNodes.remove(iq.getId());
				
				confirmingNode.setModel(nodeAdded.getModel());
				confirmingNode.setRegistrationCode(null);
				confirmingNode.setConfirmed(true);
				nodes.put(confirmingNode.getLanId(), confirmingNode);
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("Node which's thing ID is {} and LAN ID is {} has been added.",
						confirmingNode.getThingId(), confirmingNode.getLanId());
			}
			
			for (Listener listener : listeners) {
				listener.nodeAdded(confirmingNode.getLanId(), node);
			}
		}
		
		@Override
		public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
			try {
				if (logger.isErrorEnabled()) {
					logger.error("Stanza error receiving when requesting to add node to concentrator. Stanz error: '{}'.",
							error.getDefinedCondition(), error.getText());
				}
				
				if (ItemNotFound.DEFINED_CONDITION.equals(error.getDefinedCondition())) {
					processAddNodeError(IConcentrator.AddNodeError.NO_SUCH_CONCENTRATOR, node);
				} else if (ServiceUnavailable.DEFINED_CONDITION.equals(error.getDefinedCondition())) {
					processAddNodeError(IConcentrator.AddNodeError.NOT_CONCENTRATOR, node);
				} else if (Conflict.DEFINED_CONDITION.equals(error.getDefinedCondition())) {
					processAddNodeError(IConcentrator.AddNodeError.REDUPLICATE_NODE_OR_LAN_ID, node);
				} else if (NotAcceptable.DEFINED_CONDITION.equals(error.getDefinedCondition())) {
					processAddNodeError(IConcentrator.AddNodeError.NOT_UNREGISTERED_THING, node);
				} else {
					processAddNodeError(IConcentrator.AddNodeError.UNKNOWN_ERROR, node);
				}
			} finally {				
				synchronized (nodesLock) {
					confirmingNodes.remove(error.getId());
				}
			}
			
			return true;
		}

		@Override
		public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
			if (logger.isErrorEnabled()) {
				logger.error("Timeout on node[{}, {}] addition.", node.getThingId(), node.getLanId());
			}
			
			nodes.remove(node.getLanId());
			
			processAddNodeError(IConcentrator.AddNodeError.REMOTE_SERVER_TIMEOUT, node);
			
			return true;
		}

		@Override
		public void interrupted() {
			// NO-OP
		}
		
	}
	
	@Override
	public void removeNode(int lanId) throws NodeNotFoundException {
		if (!nodes.containsKey(lanId))
			throw new NodeNotFoundException(String.format("LAN node which's LAN ID is %s not found.", lanId));
		
		synchronized (nodesLock) {
			nodes.remove(lanId);
		}
	}
	
	private void processAddNodeError(IConcentrator.AddNodeError error, LanNode node) {
		for (IConcentrator.Listener listener : listeners) {
			listener.occurred(error, node);
		}
	}
	
	@Override
	public void setNodes(Map<Integer, LanNode> nodes) {
		synchronized (nodesLock) {
			if (nodes == null) {
				this.nodes = new LinkedHashMap<>();
			} else {
				this.nodes = nodes;
			}
		}
	}
	
	@Override
	public Collection<LanNode> getNodes() {
		return nodes.values();
	}

	@Override
	public void addListener(IConcentrator.Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public IConcentrator.Listener removeListener(IConcentrator.Listener listener) {
		 if (listeners.remove(listener))
			 return listener;
		 
		 return null;
	}

	@Override
	public LanNode getNode(int lanId) {
		return nodes.get(lanId);
	}

	@Override
	public void syncNodesWithServer(SyncNodesListener syncNodesListener) {
		chatServices.getTaskService().execute(new SyncNodesTask(syncNodesListener));
	}
	
	private class SyncNodesTask implements ITask<Iq> {
		private SyncNodesListener syncNodesListener;
		
		public SyncNodesTask(SyncNodesListener syncNodesListener) {
			this.syncNodesListener = syncNodesListener;
		}
		
		@Override
		public void trigger(IUnidirectionalStream<Iq> stream) {
			stream.send(new Iq(Iq.Type.GET, new PullNodes()));
		}

		@Override
		public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
			PullNodes pullNodes = iq.getObject();
			List<Node> pulledNodes = pullNodes.getNodes();
			Map<Integer, LanNode> pulledLanNodes = new HashMap<>();
			if (pulledNodes != null) {
				for (Node node : pulledNodes) {
					LanNode lanNode = convertToLanNode(node);
					pulledLanNodes.put(lanNode.getLanId(), lanNode);
				}
			}
			
			Collection<LanNode> oldNodes = nodes.values();
			nodes = pulledLanNodes;
			
			for (LanNode lanNode : oldNodes) {
				if (!lanNode.isConfirmed() && !isConfirmed(pulledLanNodes.values(), lanNode)) {
					try {
						requestServerToAddNode(lanNode.getThingId(), lanNode.getRegistrationCode(),
								lanNode.getLanId(), lanNode.getCommunicationNet().parse(lanNode.getAddress()));
					} catch (BadAddressException e) {
						throw new RuntimeException("Why???", e);
					}
				}
			}
			
			if (syncNodesListener != null)
				syncNodesListener.nodesSynced();
			
		}
		
		private boolean isConfirmed(Collection<LanNode> pulledLanNodes, LanNode lanNode) {
			for (LanNode pulledLanNode : pulledLanNodes) {
				if (pulledLanNode.getThingId().equals(lanNode.getThingId()))
					return true;
			}
			
			return false;
		}

		private LanNode convertToLanNode(Node node) {
			LanNode lanNode = new LanNode();
			lanNode.setThingId(node.getThingId());
			lanNode.setLanId(node.getLanId());
			lanNode.setModel(node.getModel());
			lanNode.setCommunicationNet(convertToCommunicationNet(node.getCommunicationNet()));
			lanNode.setAddress(node.getAddress());
			lanNode.setConfirmed(true);
			
			return lanNode;
		}

		private CommunicationNet convertToCommunicationNet(String sCommunicationNet) {
			return CommunicationNet.valueOf(sCommunicationNet);
		}

		@Override
		public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
			if (syncNodesListener != null)
				syncNodesListener.occurred(error);
			
			return true;
		}

		@Override
		public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
			if (syncNodesListener != null)
				syncNodesListener.occurred(new RemoteServerTimeout("Pull nodes timeout."));
			
			return true;
		}

		@Override
		public void interrupted() {
			// NOOP
		}
		
	}

	@Override
	public int getBestSuitedNewLanId() {
		synchronized (nodesLock) {
			List<Integer> lanIds = new ArrayList<>();
			for (LanNode node : nodes.values()) {
				if (node.isConfirmed()) {
					lanIds.add(node.getLanId());
				}
			}
			
			Collections.sort(lanIds);
			
			int i = 1;
			Integer lanId = null;
			for (int nodeLanId : lanIds) {
				if (nodeLanId != i) {
					lanId = i;
					break;
				}
				
				i++;
			}
			
			if (lanId == null) {
				lanId = i;
			}
			
			return lanId;
		}
	}

	@Override
	public String getThingName() {
		return chatServices.getStream().getJid().getNode();
	}

	@Override
	public void cleanNodes() {
		synchronized (nodesLock) {
			nodes.clear();
		}
	}

	@Override
	public void setAddNodeTimeout(long addNodeTimeout) {
		this.addNodeTimeout = addNodeTimeout;
	}

	@Override
	public long getAddNodeTimeout() {
		return addNodeTimeout;
	}
}
