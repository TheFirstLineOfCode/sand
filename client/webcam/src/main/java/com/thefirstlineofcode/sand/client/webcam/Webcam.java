package com.thefirstlineofcode.sand.client.webcam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.sand.protocols.webrtc.signaling.Signal;

public class Webcam extends AbstractWebrtcPeer implements IWebcam, IWebrtcPeer.Listener,
			IWebcamWebrtcNativeClient.Listener {
	private static final Logger logger = LoggerFactory.getLogger(Webcam.class);
	
	private boolean started;
	private boolean opened;
	
	private boolean notStartWebrtcNativeService;
	private String webrtcNativeServicePath;
	private Capability requestedCapability;
	private Capability openedCapability;
	
	private IWebcamWebrtcNativeClient nativeClient;
	
	public Webcam(IChatServices chatServices) {
		super(chatServices);
		
		started = false;
		opened = false;
		
		notStartWebrtcNativeService = false;
	}
	
	public void setWebrtcNativeServicePath(String webrtcNativeServicePath) {
		this.webrtcNativeServicePath = webrtcNativeServicePath;
	}
	
	public void setNotStartWebrtcNativeService(boolean notStartWebrtcNativeService) {
		this.notStartWebrtcNativeService = notStartWebrtcNativeService;
	}
	
	@Override
	public void start() {
		super.start();
		addListener(this);
		
		nativeClient = new WebcamWebrtcNativeClient(this);
		if (webrtcNativeServicePath != null)
			nativeClient.setNativeServicePath(webrtcNativeServicePath);
		
		if (!notStartWebrtcNativeService) {
			logger.info("Try to start WebRTC native service.");
			
			nativeClient.startNativeService();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			logger.info("WebRTC Native service has started.");
		}
		
		logger.info("Try to connect to WebRTC native service.");
		nativeClient.connect();
		logger.info("Webcam has connected to WebRTC native service.");
		
		started = true;		
		logger.info("Webcam has started.");
	}

	@Override
	public void stop() {
		super.stop();
		
		if (isOpened())
			close();
		
		if (nativeClient != null) {
			nativeClient.stopNativeService();
			nativeClient.removeListener();
			nativeClient = null;
		}
		
		removeListener(this);
		
		opened = false;
		started = false;
		
		logger.info("Webcam has stopped.");
	}
	
	@Override
	public boolean isStarted() {
		return started;
	}
	
	@Override
	public boolean isStopped() {
		return !started;
	}
	
	@Override
	public void open() {
		if (!started)
			throw new IllegalStateException("Try to open webcam which's not in started state.");
		
		nativeClient.send("OPEN " + String.format("%,%,%", requestedCapability.width,
				requestedCapability.height, requestedCapability.maxFps));
	}
	
	@Override
	public void close() {
		if (!started)
			throw new IllegalStateException("Try to open webcam which's not in started state.");
		
		if (!isOpened()) {
			logger.warn("Try to close webcam which's not in opened state.");
			return;
		}
		
		nativeClient.send("CLOSE");
	}
	
	@Override
	public boolean isOpened() {
		return opened;
	}
	
	@Override
	public boolean isClosed() {
		return !opened;
	}

	@Override
	public void offered(String offerSdp) {
		nativeClient.send("OFFER " + offerSdp);
	}

	@Override
	public void processNativeMessage(String id, String data) {
		logger.info("Received a message from WebRTC native service." +
				"Message ID: {}. Message data: {}.", id, data);
		if ("CONFLICT".equals(id) && data == null) {
			started = false;
			stop();
			
			throw new RuntimeException("Can't connect to WebRTC native service. Conflicted!");
		}
		
		if (!started)
			throw new IllegalStateException("Can't process native message. Not in started state.");
		
		if ("OPENED".equals(id) && data == null) {
			opened = true;
			sendToPeer(new Signal(Signal.ID.OPENED));
		} else if ("CLOSED".equals(id) && data == null) {
			opened = false;
			sendToPeer(new Signal(Signal.ID.CLOSED));
			peer = null;
		} else if ("ANSWER".equals(id) && data != null) {
			sendToPeer(new Signal(Signal.ID.ANSWER, data));
		}  else if ("ICE_CANDIDATE_FOUND".equals(id) && data != null) {
			sendToPeer(new Signal(Signal.ID.ICE_CANDIDATE_FOUND, data));
		} else {
			throw new RuntimeException(
					"Received a message from WebRTC native service." +
					" But the message can't be understanded." +
					String.format(" Message ID: %s. Message data: %s.", id, data));
		}
	}

	@Override
	public void answered(String answerSdp) {
		throw new IllegalStateException("Webcam received a answer SDP from the peer. Why???");
	}

	@Override
	public void iceCandidateFound(String jsonCandidate) {
		nativeClient.send("ICE_CANDIDATE_FOUND " + jsonCandidate);
	}

	protected void askToOpen(JabberId asker) {
		if (requestedCapability == null) {
			if (logger.isErrorEnabled())
				logger.error("Null requested capability.");
			
			throw new IllegalArgumentException("Null requested capability.");
		}
		
		if (peer != null && !peer.equals(asker)) {
			Iq closed = new Iq(Iq.Type.SET);
			closed.setFrom(chatServices.getStream().getJid());
			closed.setTo(peer);
			closed.setObject(new Signal(Signal.ID.CLOSED));
			
			chatServices.getIqService().send(closed);	
		}
		peer = asker;
		
		nativeClient.send(String.format("OPEN  %d,%d,%d", requestedCapability.width,
				requestedCapability.height, requestedCapability.maxFps));
	}

	protected void askToClose() {
		nativeClient.send("CLOSE");
	}
	
	protected void processPeerSignal(Iq iq, Signal.ID id, String data) {
		if (id == Signal.ID.OPEN) {
			logger.info("Received OPEN signal.");
			askToOpen(iq.getFrom());
		} else if (id == Signal.ID.CLOSE) {
			logger.info("Received CLOSE signal.");
			askToClose();
		} else {
			super.processPeerSignal(iq, id, data);
		}
		
	}

	@Override
	public void setRequestedCapability(Capability requestedCapability) {
		this.requestedCapability = requestedCapability;
	}

	@Override
	public Capability getRequestedCapability() {
		return requestedCapability;
	}

	@Override
	public Capability getOpenedCapability() {
		if (!opened)
			throw new IllegalStateException("Don't call this method before webcam opened.");
		
		return openedCapability;
	}
}
