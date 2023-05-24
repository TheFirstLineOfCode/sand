package com.thefirstlineofcode.sand.emulators.lora.gateway.log;

import java.awt.event.WindowEvent;

import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationListener;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.client.thing.obx.ObxData;
import com.thefirstlineofcode.sand.emulators.commons.ui.AbstractLogConsolePanel;
import com.thefirstlineofcode.sand.emulators.lora.things.AbstractLoraThingEmulator;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class ThingLogConsolePanel extends AbstractLogConsolePanel
		implements ICommunicationListener<LoraAddress, LoraAddress, byte[]> {
	private static final long serialVersionUID = 506009089461387655L;

	public ThingLogConsolePanel(AbstractLoraThingEmulator thing) {
		((ICommunicator<LoraAddress, LoraAddress, byte[]>)thing.getCommunicator()).addCommunicationListener(this);
	}

	@Override
	protected void doWindowClosing(WindowEvent e) {
		// No-Op
		
	}

	@Override
	public void sent(LoraAddress to, byte[] data) {
		ObxData obmData = new ObxData(toObject(data), toXml(data), data);
		log(String.format("-->%s\n" +
				"    O: %s\n" +
				"    X(%d bytes): %s\n" +
				"    B(%d bytes): %s",
				to, obmData.getProtocolObjectInfoString(),
				obmData.getXml().length(), obmData.getXml(),
		obmData.getBinary().length, obmData.getHexString()));
	}

	@Override
	public void received(LoraAddress from, byte[] data) {
		ObxData obmData = new ObxData(toObject(data), toXml(data), data);
		log(String.format("<--%s\n" +
				"    O: %s\n" +
				"    X(%d bytes): %s\n" +
				"    B(%d bytes): %s",
				from, obmData.getProtocolObjectInfoString(),
				obmData.getXml().length(), obmData.getXml(),
				obmData.getBinary().length, obmData.getHexString()));
	}

	@Override
	public void occurred(CommunicationException e) {
		log(e);
	}
	
	public void thingRemoved(AbstractLoraThingEmulator thing) {
		((ICommunicator<LoraAddress, LoraAddress, byte[]>)thing.getCommunicator()).removeCommunicationListener(this);
	}

	@Override
	public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {
		log(String.format("D(%s)<=N, D(%s)=>N", oldAddress, newAddress));
	}
}
