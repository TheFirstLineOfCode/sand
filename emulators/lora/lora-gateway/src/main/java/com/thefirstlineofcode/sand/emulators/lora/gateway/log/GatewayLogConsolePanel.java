package com.thefirstlineofcode.sand.emulators.lora.gateway.log;

import java.awt.event.WindowEvent;

import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationListener;
import com.thefirstlineofcode.sand.client.thing.obx.ObxData;
import com.thefirstlineofcode.sand.emulators.commons.ui.AbstractLogConsolePanel;
import com.thefirstlineofcode.sand.emulators.lora.network.LoraCommunicator;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class GatewayLogConsolePanel extends AbstractLogConsolePanel implements ICommunicationListener<LoraAddress, LoraAddress, byte[]> {
	private static final long serialVersionUID = -75294176722668481L;
	
	private LoraCommunicator communicator;

	public GatewayLogConsolePanel(LoraCommunicator communicator) {
		this.communicator = communicator;
		communicator.addCommunicationListener(this);
	}

	@Override
	protected void doWindowClosing(WindowEvent e) {
		communicator.removeCommunicationListener(this);
	}

	@Override
	public void sent(LoraAddress to, byte[] data) {
		Object obj = toObject(data);
		ObxData obmData = new ObxData(obj, toXml(data), data);
		log(String.format("-->%s\n" +
						"    O: %s\n" +
						"    X(%d bytes): %s\n" +
						"    B(%d bytes): %s",
				to, obmData.getProtocolObjectInfoString(), obmData.getBinary().length,
				obmData.getXml(), obmData.getXml().length(), obmData.getHexString()));
	}

	@Override
	public void received(LoraAddress from, byte[] data) {
		ObxData obmData = new ObxData(toObject(data), toXml(data), data);
		log(String.format("<--%s\n" +
						"    O: %s\n" +
						"    X(%d bytes): %s\n" +
						"    B(%d bytes): %s",
				from, obmData.getProtocolObjectInfoString(), obmData.getBinary().length,
				obmData.getXml(), obmData.getXml().length(), obmData.getHexString()));
	}

	@Override
	public void occurred(CommunicationException e) {
		log(e);
	}

	@Override
	public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {
		log(String.format("G(%s)<=N, G(%s)=>N", oldAddress.getAddressBytes(), newAddress.getAddressBytes()));
	}
}
