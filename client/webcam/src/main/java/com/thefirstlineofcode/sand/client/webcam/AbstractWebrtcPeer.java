package com.thefirstlineofcode.sand.client.webcam;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.protocols.webrtc.signaling.Signal;

public abstract class AbstractWebrtcPeer implements IWebrtcPeer, IIqListener {
	private static final Logger logger = LoggerFactory.getLogger(AbstractWebrtcPeer.class);
	
	protected IChatServices chatServices;
	
	protected List<Listener> listeners;
	protected JabberId peer;
	
	protected boolean started;
	
	public AbstractWebrtcPeer(IChatServices chatServices) {
		this(chatServices, null);
	}
	
	public AbstractWebrtcPeer(IChatServices chatServices, JabberId peer) {
		this.chatServices = chatServices;
		this.peer = peer;
		
		listeners = new ArrayList<>();
		started = false;
	}
	
	@Override
	public void start() {
		chatServices.getIqService().addListener(Signal.PROTOCOL, this);
		logger.info(String.format("WebRTC peer[%s] has started.", getClass().getSimpleName()));
		
		started = true;
	}
	
	@Override
	public boolean isStarted() {
		return started;
	}
	
	@Override
	public void stop() {
		chatServices.getIqService().removeListener(Signal.PROTOCOL);
		peer = null;
		logger.info(String.format("WebRTC peer[%s] has stopped.", getClass().getSimpleName()));
	}
	
	@Override
	public boolean isStopped() {
		return !started;
	}
	
	public void setPeer(JabberId peer) {
		this.peer = peer;
	}
	
	@Override
	public JabberId getPeer() {
		return peer;
	}

	@Override
	public void sendToPeer(Signal signal) {
		if (peer == null)
			throw new IllegalStateException("Null peer. Please set peer before sending signal to peer.");
		
		logger.info(String.format("Send signal(%s) to peer '%s'.", signal, peer));
		
		Iq iq = new Iq(Iq.Type.SET);
		iq.setTo(peer);
		iq.setObject(signal);
		
		chatServices.getIqService().send(iq);
	}
	
	@Override
	public void addListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public boolean removeListener(Listener listener) {
		return listeners.remove(listener);
	}
	
	protected void processSignal(Signal.ID signalId) {
		sendToPeer(new Signal(signalId, null));
	}
	
	protected void processSignal(Signal.ID signalId, String data) {
		sendToPeer(new Signal(signalId, data));
	}
	
	protected void processPeerSignal(Iq iq, Signal.ID id, String data) {
		logger.info(String.format("Received a signal(ID: %s, date: %s) from peer '%s'.", id, data, iq.getFrom()));
		
		if (peer != null && !peer.equals(iq.getFrom())) {
			IError error = StanzaError.create(iq, Conflict.class, "Not current peer.");
			chatServices.getStream().send(error);
			
			return;
		}
		
		if (id == Signal.ID.OFFER) {
			for (Listener listener : listeners) {
				listener.offered(data);
			}
		} else if (id == Signal.ID.ANSWER) {
			for (Listener listener : listeners) {
				listener.answered(data);
			}
		} else if (id == Signal.ID.ICE_CANDIDATE_FOUND) {
			for (Listener listener : listeners) {
				listener.iceCandidateFound(data);
			}
		} else {
			throw new RuntimeException(String.format("Unknown signal ID: %s.", id));
		}
	}

	@Override
	public void received(Iq iq) {
		if (iq.getFrom() == null)
			throw new ProtocolException(new BadRequest("Null peer address."));
		
		Signal signal = iq.getObject();
		processPeerSignal(iq, signal.getId(), signal.getData());
	}
}
