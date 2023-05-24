package com.thefirstlineofcode.sand.emulators.lora.gateway.log;

import java.awt.event.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationNetwork;
import com.thefirstlineofcode.sand.client.thing.obx.ObxData;
import com.thefirstlineofcode.sand.emulators.commons.ui.AbstractLogConsolePanel;
import com.thefirstlineofcode.sand.emulators.lora.network.ILoraNetworkListener;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class CommunicationNetworkLogConsolePanel extends AbstractLogConsolePanel implements ILoraNetworkListener {
	private static final long serialVersionUID = 4598974878913796627L;
	
	private static final Logger logger = LoggerFactory.getLogger(CommunicationNetworkLogConsolePanel.class);
	
	private ICommunicationNetwork<LoraAddress, byte[], ?> network;
	
	public CommunicationNetworkLogConsolePanel(ICommunicationNetwork<LoraAddress, byte[], ?> network) {
		super();
		
		this.network = network;
	}
	
	@Override
	protected void doWindowClosing(WindowEvent e) {
		network.removeListener(this);
	}

	@Override
	public void sent(LoraAddress from, LoraAddress to, byte[] data) {
		String logMessage = createSentLogMessage(from, to, new ObxData(toObject(data), toXml(data), data));
		
		if (logger.isDebugEnabled()) {
			logger.debug(logMessage);
		}
		
		log(logMessage);
	}

	private String createSentLogMessage(LoraAddress from, LoraAddress to, ObxData obmData) {
		return String.format("D(%s)-->N-->D(%s):" + LINE_SEPARATOR +
						"    O: %s" + LINE_SEPARATOR +
						"    X(%d bytes): %s" + LINE_SEPARATOR +
						"    B(%d bytes): %s",
				from, to, obmData.getProtocolObjectInfoString(),
				obmData.getXml().length(), obmData.getXml(),
				obmData.getBinary().length, obmData.getHexString());
	}

	@Override
	public void received(LoraAddress from, LoraAddress to, byte[] data) {
		String logMessage = createReceivedLogMessage(from, to, new ObxData(toObject(data), toXml(data), data));
		
		if (logger.isDebugEnabled()) {
			logger.debug(logMessage);
		}
		
		log(logMessage);
		
	}

	private String createReceivedLogMessage(LoraAddress from, LoraAddress to, ObxData obmData) {
		return String.format("D(%s)<--N<--D(%s)" + LINE_SEPARATOR +
						"    O: %s" + LINE_SEPARATOR +
						"    X(%d bytes): %s" + LINE_SEPARATOR +
						"    B(%d bytes): %s",
				to, from, obmData.getProtocolObjectInfoString(),
				obmData.getXml().length(), obmData.getXml(),
				obmData.getBinary().length, obmData.getHexString());
	}

	@Override
	public void addressChanged(LoraAddress newAddress, LoraAddress oldAddress) {
		String logMessage = createAddressChangedLogMessage(oldAddress, newAddress);
		
		if (logger.isInfoEnabled()) {
			logger.info(logMessage);
		}
		
		log(logMessage);
	}

	private String createAddressChangedLogMessage(LoraAddress oldAddress, LoraAddress newAddress) {
		return String.format("D(%s)<=N, D(%s)=>N", oldAddress, newAddress);
	}

	@Override
	public void collided(LoraAddress from, LoraAddress to, byte[] data) {
		String logMessage = createCollidedLogMessage(from, to, new ObxData(toObject(data), toXml(data), data));
		
		if (logger.isWarnEnabled()) {
			logger.warn(logMessage);
		}
		
		log(logMessage);
	}

	private String createCollidedLogMessage(LoraAddress from, LoraAddress to, ObxData obmData) {
		return String.format("?* D(%s)-->N-->D(%s)" + LINE_SEPARATOR +
						"    O: %s" + LINE_SEPARATOR +
						"    X(%d bytes): %s" + LINE_SEPARATOR +
						"    B(%d bytes): %s",
						from, to, obmData.getProtocolObjectInfoString(),
						obmData.getXml().length(), obmData.getXml(),
						obmData.getBinary().length, obmData.getHexString());
	}

	@Override
	public void lost(LoraAddress from, LoraAddress to, byte[] data) {
		String logMessage = createLostLogMessage(from, to, new ObxData(toObject(data), toXml(data), data));
		
		if (logger.isWarnEnabled()) {
			logger.warn(logMessage);
		}
		
		log(logMessage);
	}

	private String createLostLogMessage(LoraAddress from, LoraAddress to, ObxData obmData) {
		return String.format("?& D(%s)->N-->D(%s)" + LINE_SEPARATOR +
				"    O: %s" + LINE_SEPARATOR +
				"    X(%d bytes): %s" + LINE_SEPARATOR +
				"    B(%d bytes): %s",
				from, to, obmData.getProtocolObjectInfoString(),
				obmData.getXml().length(), obmData.getXml(),
				obmData.getBinary().length, obmData.getHexString());
	}

}
