package com.thefirstlineofcode.sand.client.lora.gateway;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator.SyncNodesListener;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacService;
import com.thefirstlineofcode.sand.client.lora.dac.LoraDacService;
import com.thefirstlineofcode.sand.client.thing.INotificationService;
import com.thefirstlineofcode.sand.client.thing.INotifier;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.lora.gateway.WorkingMode;
import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraGateway implements ILoraGateway, ILoraDacService.Listener {
	private static final Logger logger = LoggerFactory.getLogger(LoraGateway.class);
	
	private IChatServices chatServices;
	private WorkingMode workingMode;
	private boolean started;
	
	private int channels;
	private ICommunicator<LoraAddress, LoraAddress, byte[]> downlinkCommunicator;
	private List<ICommunicator<LoraAddress, LoraAddress, byte[]>> uplinkCommunicators;
	private IConcentrator concentrator;
	private ILoraDacService dacService;
	
	private byte thingCommunicationChannel;
	
	public LoraGateway(IChatServices chatServices) {
		this(chatServices, 1, null, null);
	}
	
	public LoraGateway(IChatServices chatServices, int channels,
			ICommunicator<LoraAddress, LoraAddress, byte[]> downlinkCommunicator,
			List<ICommunicator<LoraAddress, LoraAddress, byte[]>> uplinkCommunicators) {
		this.chatServices = chatServices;
		
		this.channels = channels;
		this.downlinkCommunicator = downlinkCommunicator;
		this.uplinkCommunicators = uplinkCommunicators;
		
		thingCommunicationChannel = ILoraGateway.DEFAULT_THING_COMMUNICATION_CHANNEL;
		workingMode = WorkingMode.ROUTER;
		started = false;
	}
	
	@Override
	public void start() {
		if (workingMode == null)
			workingMode = WorkingMode.ROUTER;
		
		if (logger.isInfoEnabled())
			logger.info("Starting Lora gateway....");
		
		doStart();
		started = true;
		
		if (logger.isInfoEnabled())
			logger.info("Lora gateway started.");
	}
	
	@Override
	public void stop() {
		if (logger.isInfoEnabled())
			logger.info("Stopping Lora gateway....");
		
		if (getConcentrator().isStarted())
			getConcentrator().stop();
		
		if (getDacService().isStarted())
			getDacService().stop();
		
		started = false;
		
		if (logger.isInfoEnabled())
			logger.info("Lora gateway stopped.");
	}
	
	@Override
	public boolean isStarted() {
		return started;
	}
	
	@Override
	public IConcentrator getConcentrator() {
		if (concentrator == null) {
			concentrator = chatServices.createApi(IConcentrator.class);
			if (uplinkCommunicators.size() == 1 && downlinkCommunicator.equals(uplinkCommunicators.get(0)))
				concentrator.addCommunicator(CommunicationNet.LORA, downlinkCommunicator);
			else
				concentrator.addCommunicator(CommunicationNet.LORA,
						new MultichannelLoraCommunicator(downlinkCommunicator, uplinkCommunicators));
		}
		
		return concentrator;
	}

	@Override
	public ILoraDacService getDacService() {
		if (dacService == null) {
			INotificationService notificationService = chatServices.createApi(INotificationService.class);
			INotifier notifier = notificationService.getNotifier();
			
			if (uplinkCommunicators.size() == 1 && downlinkCommunicator == uplinkCommunicators.get(0)) {
				dacService = new LoraDacService(downlinkCommunicator, thingCommunicationChannel, thingCommunicationChannel,
						DEFAULT_UPLINK_ADDRESS, getConcentrator(), notifier);
			} else {
				dacService = new LoraDacService(downlinkCommunicator, 0, uplinkCommunicators.size() - 1,
						DEFAULT_UPLINK_ADDRESS, getConcentrator(), notifier);
				dacService.setThingCommunicationChannel(thingCommunicationChannel);
			}
		}
		
		return dacService;
	}

	@Override
	public void setWorkingMode(WorkingMode workingMode) {
		if (workingMode == null)
			throw new IllegalArgumentException("Null working mode.");
		
		if (this.workingMode != workingMode) {
			this.workingMode = workingMode;
			changeWorkingMode();
		}
	}
	
	protected void doStart() {
		if (started)
			return;
		
		if (downlinkCommunicator == null)
			throw new IllegalStateException("Null downlink communicator. Call setDownlinkCommunicator first.");
		
		if (uplinkCommunicators == null || uplinkCommunicators.size() == 0)
			throw new IllegalStateException("Null or size of uplink communicators is zero. Call setUplinkCommunicator first.");
		
		try {
			if (uplinkCommunicators.size() == 1 && downlinkCommunicator.equals(uplinkCommunicators.get(0))) {
				downlinkCommunicator.changeAddress(new LoraAddress(new byte[] {DEFAULT_DOWNLINK_ADDRESS[0],
						DEFAULT_DOWNLINK_ADDRESS[1], thingCommunicationChannel}), false);
			} else {
				downlinkCommunicator.changeAddress(new LoraAddress(
						new byte[] {DEFAULT_DOWNLINK_ADDRESS[0], DEFAULT_DOWNLINK_ADDRESS[1],
								thingCommunicationChannel}), false);
				
				for (int i = 0; i < uplinkCommunicators.size(); i++) {
					uplinkCommunicators.get(i).changeAddress(new LoraAddress(
							new byte[] {DEFAULT_UPLINK_ADDRESS[0], DEFAULT_UPLINK_ADDRESS[1], (byte)i}), false);
				}
			}
		} catch (CommunicationException e) {
			throw new RuntimeException("Failed to configure lora communicators.", e);
		}
		
		IConcentrator concentrator = getConcentrator();
		concentrator.syncNodesWithServer(new SyncNodesListener() {
			
			@Override
			public void occurred(StanzaError error) {
				if (logger.isErrorEnabled())
					logger.error("The concentrator failed to pull nodes from server. Stanza error: {}.", error);
			}
			
			@Override
			public void nodesSynced() {
				if (logger.isInfoEnabled())
					logger.info("The concentrator has pulled nodes from server.");
			}
		});
		concentrator.pullLanFollows();
		
		changeWorkingMode();
	}

	private void changeWorkingMode() {
		if (logger.isInfoEnabled())
			logger.info("Change Lora gateway working mode to {}.", workingMode);
		
		if (workingMode == WorkingMode.DAC) {
			getConcentrator();
			if (concentrator.isStarted()) {
				concentrator.disableLanRouting();
			}
			
			getDacService().addListener(this);
			getDacService().start();
		} else {
			getDacService();
			if (dacService.isStarted()) {
				getDacService().removeListener(this);
				dacService.stop();
			}
			
			if (getConcentrator().isStarted()) {
				getConcentrator().enableLanRouting();
			} else {				
				getConcentrator().start();
			}
		}
	}

	@Override
	public void setDownlinkCommunicator(ICommunicator<LoraAddress, LoraAddress, byte[]> communicator) {
		this.downlinkCommunicator = communicator;
	}

	@Override
	public WorkingMode getWorkingMode() {
		return workingMode;
	}

	@Override
	public void addressConfigured(String thingId, String registrationCode, LoraAddress address) {
		IConcentrator concentrator = getConcentrator();
		concentrator.requestServerToAddNode(thingId, registrationCode, concentrator.getBestSuitedNewLanId(), address);
	}

	@Override
	public void setUplinkCommunicators(List<ICommunicator<LoraAddress, LoraAddress, byte[]>> uplinkCommunicators) {
		this.uplinkCommunicators = uplinkCommunicators;
	}
	
	@Override
	public LoraAddress[] getUplinkAddresses() {
		LoraAddress[] addresses = new LoraAddress[uplinkCommunicators.size()];
		
		for (int i = 0; i < uplinkCommunicators.size(); i++) {
			addresses[i] = new LoraAddress(DEFAULT_DOWNLINK_ADDRESS[0], DEFAULT_DOWNLINK_ADDRESS[0], (byte)i);
		}
		
		return addresses;
	}

	@Override
	public int getChannels() {
		return channels;
	}

	@Override
	public void setThingCommunicationChannel(byte thingCommunicationChannel) {
		this.thingCommunicationChannel = thingCommunicationChannel;
	}
}
