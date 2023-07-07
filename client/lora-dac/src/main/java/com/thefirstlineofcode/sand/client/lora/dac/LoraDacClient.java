package com.thefirstlineofcode.sand.client.lora.dac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.binary.BxmppConversionException;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
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
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraDacClient implements ILoraDacClient, ICommunicationListener<LoraAddress, LoraAddress, byte[]> {
	public enum State {
		INITIAL,
		INTRODUCTING,
		ALLOCATED,
		CONFIGURED
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LoraDacClient.class);
	
	private ICommunicator<LoraAddress, LoraAddress, byte[]> communicator;
	private LoraAddress dacServiceAddress;
	private Listener listener;
	private ICommunicationListener<LoraAddress, LoraAddress, byte[]> currentCommunicationListener;
	private State state;
	
	public LoraDacClient() {
		this(null);
	}
	
	public LoraDacClient(ICommunicator<LoraAddress, LoraAddress, byte[]> communicator) {
		if (communicator != null)
			setCommunicator(communicator);
		
		dacServiceAddress = DEFAULT_DAC_SERVICE_ADDRESS;
		state = State.INITIAL;
	}
	
	public void setDacServiceAddress(LoraAddress dacServiceAddress) {
		this.dacServiceAddress = dacServiceAddress;
	}
	
	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	@Override
	public void removeListener() {
		listener = null;
	}

	@Override
	public void introduce(String thingId, String registrationCode) {
		changeToInitialDacClientAddress();
		currentCommunicationListener = new IntroduceListener(thingId);
		
		communicator.startToListen();
		try {
			communicator.send(dacServiceAddress, ObxFactory.getInstance().toBinary(
					new Introduction(communicator.getAddress().getBytes(), thingId,
							registrationCode)));
			state = State.INTRODUCTING;
		} catch (CommunicationException e) {
			processCommunicationException(e);
		}
	}

	private void changeToInitialDacClientAddress() {
		try {
			communicator.changeAddress(new LoraAddress(new byte[] {(byte)0xef, (byte)0xee,
					DEFAULT_DAC_SERVICE_CHANNEL}), false);
		} catch (Exception e) {
			throw new RuntimeException("Can't change communicator's address.", e);
		}
	}

	private void processCommunicationException(CommunicationException e) {
		if (logger.isErrorEnabled())
			logger.error("Communication exception occurred.", e);
		
		if (listener != null)
			listener.occurred(e);
	}
	
	private class IntroduceListener implements ICommunicationListener<LoraAddress, LoraAddress, byte[]> {
		private String thingId;
		
		public IntroduceListener(String thingId) {
			this.thingId = thingId;
		}
		
		@Override
		public void sent(LoraAddress to, byte[] data) {}

		@Override
		public void received(LoraAddress from, byte[] data) {
			IObxFactory obxFactory = ObxFactory.getInstance();
			Protocol protocol = obxFactory.readProtocol(data);
			if (Allocation.PROTOCOL.equals(protocol) && state == State.INTRODUCTING) {
				try {
					Allocation allocation = (Allocation)obxFactory.toObject(data);
					if (listener != null) {
						listener.allocated(new LoraAddress(allocation.getGatewayUplinkAddress()),
								new LoraAddress(allocation.getGatewayDownlinkAddress()),
								new LoraAddress(allocation.getAllocatedAddress()));
					}
				} catch (BxmppConversionException e) {
					throw new RuntimeException("????Illegal allocation protocol data!", e);
				}
				
				try {
					communicator.send(dacServiceAddress, ObxFactory.getInstance().toBinary(
							new Allocated(thingId)));
					state = State.ALLOCATED;
				} catch (CommunicationException e) {
					processCommunicationException(e);
				}
			} else if (Configured.PROTOCOL.equals(protocol) && state == State.ALLOCATED) {
				if (listener != null)
					listener.configured();				
			} else {
				throw new RuntimeException(String.format("Unkown protocol or illegal DAC state. Message: %s. DAC state: %s.",
						BinaryUtils.getHexStringFromBytes(data), state));
			}
		}

		@Override
		public void occurred(CommunicationException e) {
			processCommunicationException(e);
		}

		@Override
		public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {}
		
	}
	
	@Override
	public void isConfigured(String thingId) {
		changeToInitialDacClientAddress();		
		currentCommunicationListener = new IsConfiguredListener();
		
		state = State.ALLOCATED;
		communicator.startToListen();
		try {
			communicator.send(dacServiceAddress, ObxFactory.getInstance().toBinary(
					new IsConfigured(communicator.getAddress().getBytes(), thingId)));
		} catch (CommunicationException e) {
			processCommunicationException(e);
		}
	}
	
	private class IsConfiguredListener implements ICommunicationListener<LoraAddress, LoraAddress, byte[]> {

		@Override
		public void sent(LoraAddress to, byte[] data) {}

		@Override
		public void received(LoraAddress from, byte[] data) {
			Protocol protocol = ObxFactory.getInstance().readProtocol(data);
			if (Configured.PROTOCOL.equals(protocol) && state == State.ALLOCATED) {
				if (listener != null)
					listener.configured();
			} else if (NotConfigured.PROTOCOL.equals(protocol) && state == State.ALLOCATED) {
				if (listener != null)
					listener.notConfigured();
			} else {
				throw new RuntimeException(String.format("Unkown protocol or illegal DAC state. Message: %s. DAC state: %s.",
						BinaryUtils.getHexStringFromBytes(data), state));
			}
		}

		@Override
		public void occurred(CommunicationException e) {
			processCommunicationException(e);
		}

		@Override
		public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {}
		
	}
	
	@Override
	public void setCommunicator(ICommunicator<LoraAddress, LoraAddress, byte[]> communicator) {		
		this.communicator = communicator;
		communicator.addCommunicationListener(this);
	}

	@Override
	public void introduce() {
		throw new UnsupportedOperationException("Please call introduce() with address and thing ID parameters.");
	}

	@Override
	public void negotiate(LoraAddress peerAddress, byte[] data) {
		throw new UnsupportedOperationException("Why does the code go to here???");
	}

	@Override
	public void reset() {
		removeListener();
		if (communicator != null) {
			communicator.removeCommunicationListener(this);
			communicator.stopToListen();
		}
		state = State.INITIAL;
	}

	@Override
	public void sent(LoraAddress to, byte[] data) {}

	@Override
	public void received(LoraAddress from, byte[] data) {
		if (currentCommunicationListener != null)
			currentCommunicationListener.received(from, data);
	}

	@Override
	public void occurred(CommunicationException e) {
		if (currentCommunicationListener != null)
			currentCommunicationListener.occurred(e);
	}

	@Override
	public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {}
}
