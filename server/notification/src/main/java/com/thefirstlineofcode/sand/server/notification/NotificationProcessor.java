package com.thefirstlineofcode.sand.server.notification;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.granite.framework.core.repository.IInitializable;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class NotificationProcessor implements IXepProcessor<Iq, Notification>,
			INotificationDispatcher, IInitializable, IConfigurationAware {
	private static final String CONFIGURATION_KEY_ACKED_NOTIFICATIONS_CACHE_SIZE = "acked.notifications.cache.size";
	private static final int DEFAULT_ACKED_NOTIFICATIONS_CACHE_SIZE = 1024 * 4;
	
	private Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);
	
	@Dependency(INotificationDispatcher.NAME_APP_COMPONENT_NOTIFICATION_DISPATCHER)
	private NotificationDispatcher notificationDispatcher;
	
	private Map<String, Object> ackedNotifications;
	private int ackedNotificationsCacheSize;
	
	private List<INotificationListener> notificationListeners;
	private Map<Class<?>, List<IEventListener<?>>> eventToListeners;
	
	@BeanDependency
	private IThingManager thingManager;
	
	public NotificationProcessor() {
		notificationListeners = new ArrayList<>();
		eventToListeners = new HashMap<>();
		
		ackedNotifications = new LinkedHashMap<String, Object>() {
			private static final long serialVersionUID = 6851317994502427107L;
			
			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<String, Object> eldest) {
				return size() > ackedNotificationsCacheSize;
			}
		};
		ackedNotificationsCacheSize = DEFAULT_ACKED_NOTIFICATIONS_CACHE_SIZE;
	}
	
	@Override
	public void init() {
		notificationDispatcher.setReal(this);
	}
	
	@Override
	public void process(IProcessingContext context, Iq iq, Notification notification) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		JabberId notifier = getNotifier(context, iq);
		
		if (notification.isAckRequired()) {
			Iq ack = new Iq(Iq.Type.RESULT, iq.getId());
			context.write(ack);
		}
		
		if (ackedNotifications.containsKey(iq.getId()))
			return;
		
		ackedNotifications.put(iq.getId(), null);
		
		for (INotificationListener notificationListener : notificationListeners) {
			try {				
				notificationListener.notified(context, iq, notifier, notification);
			} catch (Exception e) {
				if (logger.isErrorEnabled())
					logger.error("Error occurred when notifing event to notification listener.", e);
			}
		}
		
		Object event = notification.getEvent();
		List<IEventListener<?>> eventListeners = eventToListeners.get(event.getClass());
		
		if (eventListeners == null)
			return;
		
		for (IEventListener<?> eventListener : eventListeners) {
			try {				
				eventReceived(context, eventListener, notifier, event);
			} catch (Exception e) {
				if (logger.isErrorEnabled())
					logger.error("Error occurred when notifing event to event listener.", e);
			}
		}
	}
	
	private JabberId getNotifier(IProcessingContext context, Iq iq) {
		JabberId notifier = iq.getFrom();
		
		if (notifier == null)
			return context.getJid();
		
		JabberId sender = context.getJid();
		if (sender.getResource() != null && !ThingIdentity.DEFAULT_RESOURCE_NAME.equals(sender.getResource()))
			throw new ProtocolException(new BadRequest("Isn't the sender a edge thing?"));
		
		if (!sender.getBareId().equals(iq.getFrom().getBareId()))
			throw new ProtocolException(new BadRequest(String.format("Illegal notifier '%s' sent by '%s'.", notifier, sender)));
			
		return iq.getFrom();
	}

	@SuppressWarnings("unchecked")
	private <T> void eventReceived(IProcessingContext context, IEventListener<T> eventListener, JabberId notifier, Object event) {
		eventListener.eventReceived(context, notifier, (T)event);
	}
	
	@Override
	public synchronized <T> void addEventListener(Class<T> eventType, IEventListener<T> eventListener) {
		List<IEventListener<?>> eventListeners = eventToListeners.get(eventType);
		if (eventListeners == null) {
			eventListeners = new ArrayList<>();
			eventToListeners.put(eventType, eventListeners);
		}
		
		if (!eventListeners.contains(eventListener))
			eventListeners.add(eventListener);
	}

	@Override
	public synchronized <T> boolean removeEventListener(Class<T> eventType, IEventListener<T> eventListener) {
		List<IEventListener<?>> notificationListeners = eventToListeners.get(eventType);
		if (notificationListeners == null)
			return false;
		
		boolean removed = notificationListeners.remove(eventListener);
		if (notificationListeners.isEmpty())
			eventToListeners.remove(eventType);
		
		return removed;
	}

	@Override
	public void addNotificationListener(INotificationListener notificationListener) {
		notificationListeners.add(notificationListener);
	}

	@Override
	public boolean removeNotificationListener(INotificationListener notificationListener) {
		return notificationListeners.remove(notificationListener);
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		ackedNotificationsCacheSize = configuration.getInteger(CONFIGURATION_KEY_ACKED_NOTIFICATIONS_CACHE_SIZE,
				DEFAULT_ACKED_NOTIFICATIONS_CACHE_SIZE);
	}
}
