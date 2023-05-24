package com.thefirstlineofcode.sand.client.lora.gateway;

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
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.lora.gateway.WorkingMode;
import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraGateway implements ILoraGateway, ILoraDacService.Listener {
	private static final Logger logger = LoggerFactory.getLogger(LoraGateway.class);
	
	private IChatServices chatServices;
	private WorkingMode workingMode;
	private boolean started;
	private ICommunicator<LoraAddress, LoraAddress, byte[]> communicator;
	private IConcentrator concentrator;
	private ILoraDacService<LoraAddress> dacService;
	
	public LoraGateway(IChatServices chatServices) {
		this(chatServices, null);
	}
	
	public LoraGateway(IChatServices chatServices, ICommunicator<LoraAddress, LoraAddress, byte[]> communicator) {
		this.chatServices = chatServices;
		this.communicator = communicator;
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
			concentrator.addCommunicator(CommunicationNet.LORA, communicator);
		}
		
		return concentrator;
	}

	@Override
	public ILoraDacService<LoraAddress> getDacService() {
		if (dacService == null) {
			INotificationService notificationService = chatServices.createApi(INotificationService.class);
			INotifier notifier = notificationService.getNotifier();
			dacService = new LoraDacService(communicator, getConcentrator(), notifier);
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
		
		if (communicator == null)
			throw new IllegalStateException("Null communicator. Call setCommunicator first.");
		
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
	public void setCommunicator(ICommunicator<LoraAddress, LoraAddress, byte[]> communicator) {
		this.communicator = communicator;
		
		getDacService().setCommunicator(communicator);
		getConcentrator().addCommunicator(CommunicationNet.LORA, communicator);
	}

	@Override
	public WorkingMode getWorkingMode() {
		return workingMode;
	}

	@Override
	public void addressConfigured(String thingId, LoraAddress address) {
		IConcentrator concentrator = getConcentrator();
		concentrator.requestServerToAddNode(thingId, concentrator.getBestSuitedNewLanId(), address);
	}

}
