package com.thefirstlineofcode.sand.emulators.lora.gateway.log;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationNetwork;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationNetworkListener;
import com.thefirstlineofcode.sand.emulators.commons.ui.AbstractLogConsolesDialog;
import com.thefirstlineofcode.sand.emulators.lora.network.LoraCommunicator;
import com.thefirstlineofcode.sand.emulators.lora.things.AbstractLoraThingEmulator;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LogConsolesDialog extends AbstractLogConsolesDialog {
	private static final long serialVersionUID = 5197344780011371803L;
	
	public static final String NAME_COMMUNICATION_NETWORK = "Communication Network";
	public static final String NAME_GATEWAY = "Gateway";
	
	private ICommunicationNetwork<LoraAddress, byte[], ?> network;
	private LoraCommunicator gatewayCommunicator;
	private Map<String, List<AbstractLoraThingEmulator>> allThings;
	
	public LogConsolesDialog(JFrame parent,  ICommunicationNetwork<LoraAddress, byte[], ?> network,
			LoraCommunicator gatewayCommunicator, Map<String, List<AbstractLoraThingEmulator>> allThings) {
		super(parent);
		
		this.network = network;
		this.gatewayCommunicator = gatewayCommunicator;
		this.allThings = allThings;
		
		createPreinstlledLogConsoles();
	}

	protected void createPreinstlledLogConsoles() {
		createInternetLogConsole();
		createCommunicationNetworkLogConsole(network);
		createGatewayConsole(gatewayCommunicator);
		createThingLogConsoles(allThings);
	}

	private void createThingLogConsoles(Map<String, List<AbstractLoraThingEmulator>> allThings) {
		for (List<AbstractLoraThingEmulator> things : allThings.values()) {
			for (AbstractLoraThingEmulator thing : things) {
				createThingLogConsole(thing);
			}
		}
	}

	public void createThingLogConsole(AbstractLoraThingEmulator thing) {
		createLogConsole(thing.getThingId(), new ThingLogConsolePanel(thing));
	}

	private void createGatewayConsole(LoraCommunicator gatewayCommunicator) {
		createLogConsole(NAME_GATEWAY, new GatewayLogConsolePanel(gatewayCommunicator));
	}

	@SuppressWarnings("unchecked")
	private void createCommunicationNetworkLogConsole(ICommunicationNetwork<LoraAddress, byte[], ?> network) {
		createLogConsole(NAME_COMMUNICATION_NETWORK, new CommunicationNetworkLogConsolePanel(network));
		network.addListener((ICommunicationNetworkListener<LoraAddress, byte[]>)logConsoles.get(NAME_COMMUNICATION_NETWORK));
	}
	
	public void removeThingLogConsole(AbstractLoraThingEmulator thing) {
		ThingLogConsolePanel logConsole = (ThingLogConsolePanel)logConsoles.remove(thing.getThingId());
		tabbedPane.remove(logConsole);
	}
	
	public void thingRemoved(AbstractLoraThingEmulator thing) {
		ThingLogConsolePanel logConsole = (ThingLogConsolePanel)logConsoles.remove(thing.getThingId());
		logConsole.thingRemoved(thing);
	}
}
