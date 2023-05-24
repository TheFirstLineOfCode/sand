package com.thefirstlineofcode.sand.emulators.lora.things;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.thefirstlineofcode.basalt.oxm.binary.BxmppConversionException;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacClient;
import com.thefirstlineofcode.sand.client.lora.dac.LoraDacClient;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.client.thing.obx.ObxFactory;
import com.thefirstlineofcode.sand.emulators.commons.AbstractCommunicationNetworkThingEmulator;
import com.thefirstlineofcode.sand.emulators.lora.network.LoraChipCreationParams;
import com.thefirstlineofcode.sand.emulators.lora.network.LoraCommunicatorFactory;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.actuator.LanExecution;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanAnswer;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanNotification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public abstract class AbstractLoraThingEmulator extends AbstractCommunicationNetworkThingEmulator<LoraAddress, LoraAddress> {
	public enum DacState {
		INITIAL,
		ALLOCATED,
		CONFIGURED
	}
	
	protected ILoraDacClient dacClient;
	
	protected DacState dacState;
	protected LoraAddress gatewayUplinkAddress;
	protected LoraAddress gatewayDownlinkAddress;
	protected LoraAddress allocatedAddress;
	
	protected Integer lanId;
	
	protected Map<LanNotification, RexStrategy> notifications;
	
	protected Lock lock;
	
	protected Thread ackRequiredLanNotificationSender;
	
	public AbstractLoraThingEmulator(String model, ILoraDacClient dacClient) {
		super(model);
		
		dacState = DacState.INITIAL;
		this.dacClient = dacClient;
		
		notifications = new HashMap<>();
		
		lock = new ReentrantLock();
	}
	
	@Override
	protected ICommunicator<LoraAddress, LoraAddress, byte[]> createCommunicator() {
		return LoraCommunicatorFactory.getInstance().createLoraCommunicator(
				new LoraChipCreationParams(ILoraDacClient.DEFAULT_DAC_CLIENT_ADDRESS));
	}
	
	public String getThingStatus() {
		StringBuilder sb = new StringBuilder();
		if (powered) {
			sb.append("Power On. ");
		} else {
			sb.append("Power Off. ");
		}
		
		if (!isAddressConfigured()) {
			sb.append("Unconfigured").append(". ");
		} else if (lanId == null) {
			sb.append("Configured: ").append(allocatedAddress.toAddressString()).append(". ");
		} else {
			sb.append("Controlled: ").append(lanId).append(". ");
		}
		
		sb.append("Battery: ").append(batteryPower).append("%. ");
		
		sb.append("Thing ID: ").append(thingId);
		
		return sb.toString();
	}
	
	@Override
	protected void doPowerOn() {
		if (lanId != null && dacState == DacState.CONFIGURED) {
			if (!communicator.getAddress().equals(allocatedAddress)) {
				try {
					communicator.changeAddress(allocatedAddress, true);
				} catch (CommunicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Node has already added to concentrator or node has configured. Start to receive data from concentrator.
			startToReceiveData();
		} else {
			if (isAddressConfigured()) {
				throw new IllegalStateException(String.format("Node thing which's thing ID is '%s' is in a illegal state. Address has already configured, but LAN ID is still null.",
						thingId));
			}
			
			if (dacClient == null) {
				dacClient = new LoraDacClient(communicator);
			} else {				
				dacClient.reset();
				dacClient.setCommunicator(communicator);
			}
			
			if (dacState == DacState.INITIAL) {
				doDac();
			} else if (dacState == DacState.ALLOCATED) {
				askIsConfigured();
			} else {
				if (dacState == null)
					throw new RuntimeException("Null DAC state.");
			}
		}
		
		runAckRequiredLanNotificationSender();
	}

	private void runAckRequiredLanNotificationSender() {
		if (ackRequiredLanNotificationSender == null) {
			ackRequiredLanNotificationSender = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {					
						if (dacState != DacState.CONFIGURED || lanId == null) {					
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							continue;
						}
						
						try {
							if (lock.tryLock(200, TimeUnit.MILLISECONDS)) {
								List<LanNotification> timeoutNotifications = new ArrayList<>();
								try {									
									for (Entry<LanNotification, RexStrategy> entry : notifications.entrySet()) {
										RexStrategy strategy = entry.getValue();
										long nextRexTime = strategy.getNextRexTime();
										
										if (nextRexTime == -1) {
											timeoutNotifications.add(entry.getKey());
											continue;
										}
										
										if (Calendar.getInstance().getTimeInMillis() > nextRexTime) {
											sendToPeer(gatewayUplinkAddress, entry.getKey());
											strategy.retransmited();
										}
									}
									
									for (LanNotification timeoutNotification : timeoutNotifications)
										notifications.remove(timeoutNotification);
								} finally {
									lock.unlock();
								}
							}
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			});
			
			ackRequiredLanNotificationSender.start();
		}
	}

	private void askIsConfigured() {
		dacClient.setListener(new LoraDacClientListenerAdapter() {					
			@Override
			public void configured() {
				dacState = DacState.CONFIGURED;	
				toBeALanNode(1000);
			}
			
			@Override
			public void notConfigured() {
				reset();
				powerOff();
				powerOn();
			}
		});
		
		dacClient.isConfigured(thingId);
	}
	
	private void toBeALanNode(final int ms) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(ms);
				} catch (InterruptedException e) {
					throw new RuntimeException("????", e);
				}
				
				dacClient.reset();
				try {
					communicator.changeAddress(allocatedAddress, true);
					startToReceiveData();
				} catch (CommunicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void doDac() {
		dacClient.setListener(new LoraDacClientListenerAdapter() {					
			@Override
			public void configured() {
				dacState = DacState.CONFIGURED;
				toBeALanNode(1000);
			}
			
			@Override
			public void allocated(LoraAddress gatewayUplinkAddress, LoraAddress gatewayDownlinkAddress,
					LoraAddress allocatedAddress) {
				addressAllocated(gatewayDownlinkAddress, gatewayUplinkAddress, allocatedAddress);
			}
		});
		
		dacClient.introduce(thingId);
	}
	
	private abstract class LoraDacClientListenerAdapter implements ILoraDacClient.Listener {

		@Override
		public void allocated(LoraAddress gatewayUplinkAddress, LoraAddress gatewayDownlinkAddress,
				LoraAddress allocatedAddress) {}
		
		@Override
		public void notConfigured() {}

		@Override
		public void occurred(CommunicationException e) {
			e.printStackTrace();
		}
		
	}
	
	public void nodeAdded(int lanId) {
		this.lanId = lanId;
		
		if (dacState != DacState.CONFIGURED)
			dacState = DacState.CONFIGURED;
		
		getPanel().updateStatus(getThingStatus());
		
		if (isPowered()) {
			startToReceiveData();
		}
	}
	
	@Override
	protected void doPowerOff() {
		if (dacClient != null) {			
			dacClient.reset();
			dacClient = null;
		}
		
		stopDataReceving();
	}
	
	@Override
	protected void doReset() {
		lanId = null;
		
		gatewayUplinkAddress = null;
		gatewayDownlinkAddress = null;
		allocatedAddress = null;
		
		dacState = DacState.INITIAL;
	}
	
	public void addressAllocated(LoraAddress gatewayUplinkAddress, LoraAddress gatewayDownlinkAddress,
				LoraAddress allocatedAddress) {
		this.gatewayUplinkAddress = gatewayUplinkAddress;
		this.gatewayDownlinkAddress = gatewayDownlinkAddress;
		this.allocatedAddress = allocatedAddress;
		
		dacState = DacState.ALLOCATED;
		
		getPanel().updateStatus(getThingStatus());
	}
	
	public boolean isAddressConfigured() {
		return dacState == DacState.CONFIGURED;
	}

	@Override
	public void occurred(CommunicationException e) {}
	
	@Override
	public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {}
	
	protected void processLanExecution(LoraAddress from, byte[] data) {		
		LanExecution request = null;
		try {
			request = (LanExecution)ObxFactory.getInstance().toObject(data);		
			Object action = request.getAction();
			
			if (from == null)
				from = gatewayUplinkAddress;
			
			processAction(action);
			sendToPeer(from, new LanAnswer(ThingsTinyId.createResponseId(request.getTraceId())));
		} catch (ExecutionException e) {
			sendToPeer(from, new LanAnswer(ThingsTinyId.createErrorId(request.getTraceId()), e.getErrorNumber()));
		} catch (BxmppConversionException e) {
			throw new RuntimeException("Failed to convert BXMPP data to LAN execution object.", e);
		}
	}
	
	protected void notify(LanNotification notification) {
		if (notification.isAckRequired()) {
			try {
				lock.lock();				
				notifications.put(notification, new RexStrategy());
			} finally {
				lock.unlock();
			}
		} else {
			sendToPeer(gatewayUplinkAddress, notification);
		}
	}
	
	protected void processReceived(LoraAddress from, byte[] data) {
		Protocol protocol = readProtocol(data);
		if (protocol == null) {
			throw new RuntimeException(String.format("Unrecognized protocol. Data: %s.", getDataInfoString(data)));
		}
		
		if (LanAnswer.PROTOCOL.equals(protocol)) {
			processLanAnswer(from, data);
		} else {
			super.processReceived(from, data);
		}
	}
	
	private void processLanAnswer(LoraAddress from, byte[] data) {
		LanAnswer answer;
		try {
			answer = (LanAnswer)ObxFactory.getInstance().toObject(data);
		} catch (BxmppConversionException e) {
			throw new RuntimeException("Failed to convert BXMPP data to LAN answer object.", e);
		}
		
		boolean locked = false;
		try {
			locked = lock.tryLock(200, TimeUnit.MILLISECONDS);
			if (locked) {
				for (LanNotification notification : notifications.keySet()) {
					if (isAnswer(notification.getTraceId(), answer.getTraceId())) {
						notifications.remove(notification);
						return;
					}
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		
		if (!locked) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			processLanAnswer(from, data);
		}
	}

	private boolean isAnswer(byte[] requestId, byte[] answerId) {
		return ThingsTinyId.createInstance(answerId).isAnswerId(answerId);
	}

	private class RexStrategy {
		private static final int MIN_REX_INTERVAL = 1000;
		private static final int REX_SPREADING_FACTOR = 128;
		private static final int MAX_REX_TIMES = 3;
		
		private int rexTimes;
		private long nextRexTime;
		
		public RexStrategy() {
			rexTimes = 0;
			nextRexTime = Calendar.getInstance().getTimeInMillis();
		}
		
		public long getNextRexTime() {
			return nextRexTime;
		}
		
		public void retransmited() {
			rexTimes++;
			
			if (rexTimes >= MAX_REX_TIMES) {				
				nextRexTime = -1;
			} else {
				long rexCaculationBaseNumber = lanId % 8;
				rexCaculationBaseNumber += getElapsedTime() % 8;
				rexCaculationBaseNumber += new Random(Calendar.getInstance().getTimeInMillis()).nextInt(8);
				
				nextRexTime = nextRexTime + MIN_REX_INTERVAL + rexCaculationBaseNumber * REX_SPREADING_FACTOR;
			}
		}
	}
}
