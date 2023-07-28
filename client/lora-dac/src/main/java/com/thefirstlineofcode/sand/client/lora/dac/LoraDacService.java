package com.thefirstlineofcode.sand.client.lora.dac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.binary.BinaryConstants;
import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.binary.BxmppConversionException;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.thing.INotifier;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationListener;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.client.thing.obx.IObxFactory;
import com.thefirstlineofcode.sand.client.thing.obx.ObxFactory;
import com.thefirstlineofcode.sand.protocols.lora.dac.Allocated;
import com.thefirstlineofcode.sand.protocols.lora.dac.Allocation;
import com.thefirstlineofcode.sand.protocols.lora.dac.Configured;
import com.thefirstlineofcode.sand.protocols.lora.dac.Introduction;
import com.thefirstlineofcode.sand.protocols.lora.dac.IsConfigured;
import com.thefirstlineofcode.sand.protocols.lora.dac.NotConfigured;
import com.thefirstlineofcode.sand.protocols.lora.dac.Reconfigure;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraDacService implements ILoraDacService, ICommunicationListener<LoraAddress, LoraAddress, byte[]> {
	private static final byte DEFAULT_DAC_SERVICE_CHANNEL = 0x1f;
	private static final LoraAddress DEFAULT_DAC_SERVICE_ADDRESS = new LoraAddress(new byte[] {(byte)0xef, (byte)0xef, DEFAULT_DAC_SERVICE_CHANNEL});
	
	private static final byte DEFAULT_THING_COMMUNICATION_CHANNEL = 0x17;
	
	private static final int MAX_MESSAGES_SIZE = 1024 * 16;
	
	private static final String PROTOCOL_NAMESPACE_LORA_DAC = "urn:leps:tuxp:lora-dac";
	
	private static final Logger logger = LoggerFactory.getLogger(LoraDacService.class);
	
	public enum State {
		NONE,
		WAITING,
		ALLOCATING,
		ALLOCATED,
		CONFIGURED
	}
	
	private ICommunicator<LoraAddress, LoraAddress, byte[]> communicator;
	private LoraAddress oldCommunicatorAddress;
	private IConcentrator concentrator;
	private INotifier notifier;
	
	private boolean started;
	
	private int uplinkChannelBegin;
	private int uplinkChannelEnd;
	private byte[] uplinkAddress;
	
	private LoraAddress dacServiceAddress;
	private byte thingCommunicationChannel;
	
	private State state;
	private String nodeThingId;
	private String nodeRegistrationCode;
	private LoraAddress nodeIntroducedAddress;
	private LoraAddress nodeAllocatedAddress;
	
	private List<Listener> listeners;
	private Allocator addressAllocator;
	
	private byte[] messages;
	private int messagesLength;
	
	public LoraDacService(ICommunicator<LoraAddress, LoraAddress, byte[]> communicator,
			int uplinkChannelBegin, int uplinkChannelEnd, byte[] uplinkAddress,
			IConcentrator concentrator, INotifier notifier) {
		this.concentrator = concentrator;
		
		dacServiceAddress = DEFAULT_DAC_SERVICE_ADDRESS;
		thingCommunicationChannel = DEFAULT_THING_COMMUNICATION_CHANNEL;
		
		messages = new byte[MAX_MESSAGES_SIZE];
		cleanMessages();
		
		setUplinkChannelBegin(uplinkChannelBegin);
		setUplinkChannelEnd(uplinkChannelEnd);
		setUplinkAddress(uplinkAddress);
		setCommunicator(communicator);
		
		this.notifier = notifier;
		this.notifier.registerSupportedEvent(Reconfigure.class);
		
		started = false;
		state = State.NONE;
		
		listeners = new ArrayList<>();
	}
	
	public void start() {
		if (started) {
			if (logger.isInfoEnabled()) {
				logger.info("DAC service is running now. Ignore to execute 'start' command.");
			}
		
			return;
		}
		
		if (logger.isInfoEnabled()) {
			logger.info("Starting DAC service.");
		}
		
		communicator.addCommunicationListener(this);
		try {
			communicator.changeAddress(dacServiceAddress, false);
		} catch (CommunicationException e) {
			throw new RuntimeException(String.format("Can't change address to %s.", dacServiceAddress));
		}
		
		communicator.startToListen();
		
		started = true;
		
		if (logger.isInfoEnabled()) {
			logger.info("DAC service has started.");
		}
		
		introduce();
	}
	
	@Override
	public void stop() {
		if (logger.isInfoEnabled()) {
			logger.info("Stopping DAC service.");
		}
		
		if (!started) {
			logger.info("It seemed that DAC service has already is being in stopped state. Ignore to execute 'stop' command.");
			return;
		}
		
		if (communicator.isListening())
			communicator.stopToListen();
				
		communicator.removeCommunicationListener(this);
		try {
			communicator.changeAddress(oldCommunicatorAddress, false);
		} catch (CommunicationException e) {
			throw new RuntimeException("Can't change communicator's address.", e);
		}
		
		state = State.NONE;
		nodeThingId = null;
		nodeRegistrationCode = null;
		nodeAllocatedAddress = null;
		
		started = false;
		
		if (logger.isInfoEnabled()) {
			logger.info("DAC service has stopped.");
		}
	}
	
	@Override
	public void introduce() {
		if (!started)
			return;
		
		state = State.WAITING;
	}

	@Override
	public synchronized void negotiate(LoraAddress peerAddress, byte[] message)  {
		try {
			doNegotiate(peerAddress, message);
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Catched an exception when the DAC service had configured the node.", e);
			
			reset();
		}
	}

	private void doNegotiate(LoraAddress peerAddress, byte[] message) throws CommunicationException, BxmppConversionException {
		if (!started) {
			if (logger.isWarnEnabled()) {
				logger.warn("Receiving address configuration request from '{}' in stopped state.", peerAddress);
			}

			return;
		}
		
		Protocol protocol = ObxFactory.getInstance().readProtocol(message);
		if (!PROTOCOL_NAMESPACE_LORA_DAC.equals(protocol.getNamespace())) {
			if (logger.isErrorEnabled())
				logger.error("Not a DAC protocol. Abandon the message. Message: {}.", BinaryUtils.getHexStringFromBytes(message));
			return;
		}
		
		if (protocol.equals(IsConfigured.PROTOCOL)) {
			processIsConfigured(message);
			return;
		}
		
		if (nodeAllocatedAddress != null && peerAddress != null && !nodeIntroducedAddress.equals(peerAddress)) {
			processParallelAddressConfigurationRequest(peerAddress, null);
		}
		
		if (state == State.WAITING) {
			if (!Introduction.PROTOCOL.equals(protocol)) {
				processParallelAddressConfigurationRequest(peerAddress, null);
				return;
			}
			
			Introduction introduction = (Introduction)ObxFactory.getInstance().toObject(Introduction.class, message);
			nodeThingId = introduction.getThingId();
			nodeRegistrationCode = introduction.getRegistrationCode();
			nodeIntroducedAddress = new LoraAddress(introduction.getAddress());
			
			if (nodeThingId == null || nodeRegistrationCode == null) {
				if (logger.isErrorEnabled()) {
					logger.error("Null thing ID or null registration code.");
				}
				
				return;
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("Introduction protocol received. Client thing ID: '{}'. Introduced address is '{}'.",
						introduction.getThingId(), nodeIntroducedAddress.toAddressString());
			}
			
			if (concentrator.isConfigured(nodeThingId)) {
				if (logger.isErrorEnabled()) {
					logger.error("Client which's thing ID is {} tries to reconfiure itself.", introduction.getThingId());
				}
				
				notifier.notify(new Reconfigure(nodeThingId));
				return;
			}
			
			Allocation allocation;
			if (addressAllocator != null) {
				allocation = addressAllocator.allocate(this);
			} else {
				allocation = allocate();				
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("Node allocation: {}: {} => {}.", nodeThingId,
						nodeIntroducedAddress.toAddressString(), nodeAllocatedAddress.toAddressString());
			};
			
			byte[] response = ObxFactory.getInstance().toBinary(allocation);
			communicator.send(nodeIntroducedAddress, response);
			
			state = State.ALLOCATING;
			return;
		} else if (state == State.ALLOCATING) {
			if (nodeThingId == null || nodeAllocatedAddress == null)
				throw new IllegalStateException("Null node thing ID or Null node address.");
			
			if (!Allocated.PROTOCOL.equals(ObxFactory.getInstance().readProtocol(message))) {
				processParallelAddressConfigurationRequest(peerAddress, null);
				return;
			}
			
			Allocated allocated = (Allocated)ObxFactory.getInstance().toObject(Allocated.class, message);
			if (logger.isInfoEnabled()) {
				logger.info("Node which's thing ID is '{}' has allocated.", allocated.getThingId());
			}
			
			if (!nodeThingId.equals(allocated.getThingId())) {
				processParallelAddressConfigurationRequest(peerAddress, allocated.getThingId());
			}
			
			state = State.ALLOCATED;
			
			for (Listener listener : listeners) {
				listener.addressConfigured(nodeThingId, nodeRegistrationCode, nodeAllocatedAddress);
			}
			state = State.CONFIGURED;
			
			if (logger.isInfoEnabled()) {
				logger.info("Node which's thing ID is '{}' has configured.", allocated.getThingId());
			}
			
			byte[] response = ObxFactory.getInstance().toBinary(new Configured());
			communicator.send(nodeIntroducedAddress, response);
			
			reset();
		} else {
			throw new IllegalStateException(String.format("Illegal configuration state: %s.", state));
		}
	}

	protected Allocation allocate() {
		Allocation allocation = new Allocation();
		allocation.setUplinkChannelBegin(uplinkChannelBegin);
		allocation.setUplinkChannelEnd(uplinkChannelEnd);
		allocation.setUplinkAddress(uplinkAddress);
		
		int nodeLanId = concentrator.getBestSuitedNewLanId();
		nodeAllocatedAddress = new LoraAddress(new byte[] {0x0, Byte.parseByte(String.valueOf(nodeLanId)), thingCommunicationChannel});
		allocation.setAllocatedAddress(nodeAllocatedAddress.getBytes());
		
		return allocation;
	}
	
	@Override
	public void reset() {
		nodeThingId = null;
		nodeRegistrationCode = null;
		nodeIntroducedAddress = null;
		nodeAllocatedAddress = null;
		state = State.WAITING;
		
		if (logger.isInfoEnabled()) {
			logger.info("DAC service has reset.");
		}
	}

	private void processIsConfigured(byte[] data) throws CommunicationException, BxmppConversionException {
		IObxFactory obxFactory = ObxFactory.getInstance();
		IsConfigured isConfigured = (IsConfigured)obxFactory.toObject(data);
		
		if (logger.isInfoEnabled()) {
			logger.info("IsConfigured protocol received. Client thing ID: {}.", isConfigured.getThingId());
		}
		
		LoraAddress address;
		address = new LoraAddress(isConfigured.getAddress());
		
		if (concentrator.isConfigured(isConfigured.getThingId())) {
			if (logger.isInfoEnabled()) {
				logger.info("Client which's thing ID is {} has been configured. Sending Configured protocol to it.", isConfigured.getThingId() );
			}
			
			communicator.send(address, obxFactory.toBinary(new Configured()));
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Client which's thing ID is {} hasn't been configured. Sending NotConfigured protocol to it.", isConfigured.getThingId() );
			}
			
			communicator.send(address, obxFactory.toBinary(new NotConfigured()));
		}
	}
	
	private void processParallelAddressConfigurationRequest(LoraAddress peerAddress, String thingId) {
		if (logger.isErrorEnabled()) {
			logger.error("Parallel address configuration request from '{}' by thing which's thing ID is '{}'.",
					peerAddress == null ? "Unknown address" : BinaryUtils.getHexStringFromBytes(peerAddress.getAddressBytes()),
							thingId == null ? "Unknown" : thingId);
		}
		
		throw new ProtocolException(new Conflict(String.format("Parallel address configuration request from %s.", peerAddress == null ? "null" : peerAddress.getAddressBytes())));
	}

	@Override
	public void setCommunicator(ICommunicator<LoraAddress, LoraAddress, byte[]> communicator) {
		if (communicator != null) {			
			oldCommunicatorAddress = communicator.getAddress();
			thingCommunicationChannel = oldCommunicatorAddress.getChannel();
			
			this.communicator = communicator;
		}
	}
	
	public State getState() {
		return state;
	}
	
	@Override
	public void sent(LoraAddress to, byte[] data) {
		// NO-OP
	}

	@Override
	public void received(LoraAddress from, byte[] data) {
		if (!addMessage(data))
			return;
		
		processMessage();
	}

	private void processMessage() {
		int protocolStartPosition = findProtocolStartPosition();
		if (protocolStartPosition == -1) {
			cleanMessages();
			
			if (logger.isDebugEnabled())
				logger.debug("No full protocol found. Continuing to wait data.");
			
			return;
		}

		int protocolEndPosition = findProtocolEndPosition(protocolStartPosition);
		if (protocolEndPosition == -1) {
			if (logger.isDebugEnabled())
				logger.debug("No full protocol found. Continuing to wait data.");
			
			return;
		}

		try {
			negotiate(null, Arrays.copyOfRange(messages, protocolStartPosition, protocolEndPosition + 1));			
		} finally {
			if(protocolEndPosition == messagesLength - 1) {
				cleanMessages();
			} else {
				messagesLength = messagesLength - (protocolEndPosition + 1);
				System.arraycopy(messages, protocolEndPosition + 1, messages, 0, messagesLength);
			}
			
		}
	}
	
	private int findProtocolStartPosition() {
		for (int i = 0; i < messagesLength - 1; i++) {
			if (messages[i] == BinaryConstants.FLAG_DOC_BEGINNING_END) {
				if (messages[i + 1] == BinaryConstants.FLAG_DOC_BEGINNING_END)
					continue;

				return i;
			}
		}

		return -1;
	}

	private int findProtocolEndPosition(int protocolStartPosition) {
		for(int i = protocolStartPosition + 1; i < messagesLength; i++) {
			if(messages[i] == BinaryConstants.FLAG_DOC_BEGINNING_END) {
				return i;
			}
		}

		return -1;
	}

	private boolean addMessage(byte[] data) {
		if (data.length == 0)
			return false;
		
		if (data.length + messagesLength > MAX_MESSAGES_SIZE) {
			cleanMessages();
			if (logger.isInfoEnabled())
				logger.info("Messages overflow. Clean it.");
			
			return false;
		}
		
		System.arraycopy(data, 0, messages, messagesLength, data.length);
		messagesLength += data.length;
		
		return true;
	}

	private void cleanMessages() {
		messagesLength = 0;
	}

	@Override
	public void occurred(CommunicationException e) {
		// NO-OP
	}

	@Override
	public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {
		// NO-OP
	}
	
	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	@Override
	public boolean removeListener(Listener listener) {
		return listeners.remove(listener);
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isStopped() {
		return !started;
	}
	
	@Override
	public void setDacServiceAddress(LoraAddress dacServiceAddress) {
		this.dacServiceAddress = dacServiceAddress;
	}
	
	@Override
	public LoraAddress getDacServiceAddress() {
		return dacServiceAddress;
	}
	
	@Override
	public void setThingCommunicationChannel(byte thingCommunicationChannel) {
		this.thingCommunicationChannel = thingCommunicationChannel;
	}

	@Override
	public byte getThingCommunicationChannel() {
		return thingCommunicationChannel;
	}
	
	@Override
	public void setConcentrator(IConcentrator concentrator) {
		this.concentrator = concentrator;
	}
	
	@Override
	public int getUplinkChannelBegin() {
		return uplinkChannelBegin;
	}

	public void setUplinkChannelBegin(int uplinkChannelBegin) {
		this.uplinkChannelBegin = uplinkChannelBegin;
	}
	
	@Override
	public int getUplinkChannelEnd() {
		return uplinkChannelEnd;
	}

	public void setUplinkChannelEnd(int uplinkChannelEnd) {
		this.uplinkChannelEnd = uplinkChannelEnd;
	}
	
	@Override
	public void setUplinkAddress(byte[] uplinkAddress) {
		if (uplinkAddress == null || uplinkAddress.length != 2)
			throw new IllegalArgumentException("Null or illegal uplink address.");
		
		this.uplinkAddress = uplinkAddress;
	}

	@Override
	public byte[] getUplinkAddress() {
		return uplinkAddress;
	}

	@Override
	public void setAddressAllocator(Allocator addressAllocator) {
		this.addressAllocator = addressAllocator;
	}

	@Override
	public IConcentrator getConcentrator() {
		return concentrator;
	}
}
