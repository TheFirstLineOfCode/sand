package com.thefirstlineofcode.sand.client.thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;

public class Notifier implements INotifier, IRexListener {
	private static final Logger logger = LoggerFactory.getLogger(Notifier.class);
	
	private static final long DEFAULT_REX_INIT_INTERVAL = 1000 * 5;
	private static final long DEFAULT_REX_TIMEOUT = 1000 * 120;
	
	private IChatServices chatServices;
	
	private long defaultRexInitInterval = DEFAULT_REX_INIT_INTERVAL;
	private long defaultRexTimeout = DEFAULT_REX_TIMEOUT;
	
	public Notifier(IChatServices chatServices) {
		this.chatServices = chatServices;
		
		String sDefaultRexInitInterval = System.getProperty("chalk.notification.default.rex.init.interval");
		if (sDefaultRexInitInterval != null) {
			try {
				defaultRexInitInterval = Long.parseLong(sDefaultRexInitInterval);				
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Not a long integer.");
			}
		}
		
		String sDefaultRexTimeout = System.getProperty("chalk.notification.default.rex.timeout");
		if (sDefaultRexTimeout != null) {
			try {
				defaultRexTimeout = Long.parseLong(sDefaultRexTimeout);				
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Not a long integer.");
			}
		}
		
	}

	@Override
	public void notify(Object event) {
		notify(null, event);
	}
	
	@Override
	public void notify(JabberId notifier, Object event) {
		notify(notifier, new Notification(event), null, null);
	}

	@Override
	public void notifyWithAck(Object event) {
		notifyWithAck(null, event);		
	}
	
	@Override
	public void notifyWithAck(JabberId notifier, Object event) {
		notifyWithAck(notifier, event, (IAckListener)null);		
	}

	@Override
	public void notifyWithAck(Object event, IRexStrategy rexStrategy) {
		notifyWithAck(null, event, rexStrategy);
	}
	
	@Override
	public void notifyWithAck(JabberId notifier, Object event, IRexStrategy rexStrategy) {
		notify(notifier, new Notification(event, true), rexStrategy, null);
	}

	@Override
	public void notifyWithAck(Object event, IAckListener ackListener) {
		notifyWithAck(null, event, ackListener);
	}
	
	@Override
	public void notifyWithAck(JabberId notifier, Object event, IAckListener ackListener) {
		notify(notifier, new Notification(event, true), null, ackListener);
	}

	@Override
	public void notifyWithAck(Object event, IRexStrategy rexStrategy, IAckListener ackListener) {
		notifyWithAck(null, event, rexStrategy, ackListener);
	}
	
	@Override
	public void notifyWithAck(JabberId notifier, Object event, IRexStrategy rexStrategy, IAckListener ackListener) {
		notify(notifier, new Notification(event, true), rexStrategy, ackListener);
	}
	
	private void notify(JabberId notifier, Notification notification, IRexStrategy rexStrategy, IAckListener ackListener) {
		if (notification.getEvent() == null)
			throw new IllegalArgumentException("Null event.");
		
		Iq iq = new Iq(Iq.Type.SET, notification);
		if (notifier != null) {
			if (!chatServices.getStream().getJid().getBareId().equals(notifier.getBareId())) {
				throw new IllegalArgumentException(String.format("Illegal notifier JID. Sender JID: %s. Notifier JID: %s",
						chatServices.getStream().getJid(), notifier));
			}
			
			iq.setFrom(notifier);
		}
		
		if (!notification.isAckRequired()) {
			if (logger.isInfoEnabled())
				logger.info("Send a notification which's stanza ID is '{}' and which's event object is {}. No ACK required.",
						iq.getId(), notification.getEvent());
			
			chatServices.getIqService().send(iq);
			return;
		}
		
		if (rexStrategy == null)
			rexStrategy = createDefaultRexStrategy();
		
		rexStrategy.addRexListener(this);
		
		if (ackListener != null) {			
			rexStrategy.addAckListener(ackListener);
		}
		
		if (logger.isInfoEnabled())
			logger.info("Send a notification which's stanza ID is '{}' and which's event object is {}. ACK required.",
					iq.getId(), notification.getEvent());
		rexStrategy.waitAck(chatServices, iq);
		chatServices.getIqService().send(iq);
	}

	protected IRexStrategy createDefaultRexStrategy() {
		return new TimeBasedRexStrategy(defaultRexInitInterval, defaultRexTimeout);
	}

	@Override
	public void retransmit(Iq iq) {
		if (logger.isDebugEnabled())
			logger.debug("No ACK received. Retransmit the notification which's stanza ID is '{}'.", iq.getId());
		
		chatServices.getIqService().send(iq);
	}

	@Override
	public void abandon(Iq iq) {
		logger.warn("Abandon a notification which's stanza ID is '{}' because no ACK received.", iq.getId());
	}
	
	@Override
	public void acked(Iq iq) {
		if (logger.isInfoEnabled())
			logger.info("ACK for notification which's stanza ID is '{}' has received.", iq.getId());
	}

	@Override
	public void registerSupportedEvent(Class<?> eventType) {
		chatServices.getOxmFactory().register(eventType, new CocTranslatorFactory<>(eventType));
	}
}
