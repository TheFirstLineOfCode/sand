package com.thefirstlineofcode.sand.client.webcam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.sand.protocols.webrtc.signaling.Signal;

public abstract class AbstractWatcher extends AbstractWebrtcPeer implements IWatcher {
	private static final Logger logger = LoggerFactory.getLogger(AbstractWatcher.class);
	
	protected Status status;
	
	public AbstractWatcher(IChatServices chatServices, JabberId peer) {
		super(chatServices, peer);
		
		status = Status.CLOSED;
	}
	
	@Override
	protected void processSignal(Signal.ID signalId, String data) {
		if (signalId != Signal.ID.OPEN &&
				signalId != Signal.ID.CLOSE &&
				signalId != Signal.ID.OFFER &&
				signalId != Signal.ID.ICE_CANDIDATE_FOUND)
			throw new IllegalArgumentException(String.format("Signal '%s' shouldn't occurred on watcher.", signalId));
		
		super.processSignal(signalId, data);
		
		if (signalId == Signal.ID.OFFER)
			status = Status.OFFERED;
	}
	
	protected void processPeerSignal(Iq iq, Signal.ID id, String data) {
		if (id == Signal.ID.OPENED) {
			if (isOpened()) {
				logger.warn("Repeated opened signal. Ignore it.");
				return;
			}
			
			opened();
		} else if (id == Signal.ID.CLOSED) {
			if (isClosed()) {
				logger.warn("Repeated closed signal. Ignore it.");
				return;
			}
			
			closed();
		} else {
			super.processPeerSignal(iq, id, data);
			
			if (id == Signal.ID.ANSWER)
				status = Status.ANSWERED;
		}
		
	}
	
	@Override
	public void opened() {
		status = Status.OPENED;
	}
	
	@Override
	public boolean isOpened() {
		return status == Status.OPENED;
	}
	
	@Override
	public void closed() {
		if (peer != null)
			peer = null;
		
		status = Status.CLOSED;
	}
	
	@Override
	public boolean isClosed() {
		return status == Status.CLOSED;
	}
	
	@Override
	public void watch() {
		if (!isStarted())
			start();
		
		open();
	}
	
	protected void open() {
		processSignal(Signal.ID.OPEN);
	}
	
	@Override
	public void close() {
		processSignal(Signal.ID.CLOSE);
	}
	
	@Override
	public Status getStatus() {
		return status;
	}
}
