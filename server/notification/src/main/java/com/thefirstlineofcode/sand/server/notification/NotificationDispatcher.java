package com.thefirstlineofcode.sand.server.notification;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;

@AppComponent(INotificationDispatcher.NAME_APP_COMPONENT_NOTIFICATION_DISPATCHER)
public class NotificationDispatcher implements INotificationDispatcher {
	private List<INotificationListener> notificationListeners;
	private Map<Class<?>, List<IEventListener<?>>> eventToListeners;
	
	private INotificationDispatcher real;
	
	public NotificationDispatcher() {
		notificationListeners = new ArrayList<>();
		eventToListeners = new HashMap<>();
	}
	
	public synchronized void setReal(INotificationDispatcher real) {
		this.real = real;
		
		for (INotificationListener notificationListener : notificationListeners) {
			real.addNotificationListener(notificationListener);
		}
		
		for (Entry<Class<?>, List<IEventListener<?>>> entry : eventToListeners.entrySet()) {
			for (IEventListener<?> eventListener : entry.getValue()) {
				addEventListenerToReal(real, entry, eventListener);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void addEventListenerToReal(INotificationDispatcher real, Entry<Class<?>, List<IEventListener<?>>> entry,
				IEventListener<?> eventListener) {
		real.addEventListener((Class<T>)entry.getKey(), (IEventListener<T>)eventListener);
	}

	@Override
	public synchronized <T> void addEventListener(Class<T> eventType, IEventListener<T> eventListener) {
		if (real == null) {
			List<IEventListener<?>> eventListeners = eventToListeners.get(eventType);
			if (eventListeners == null) {
				eventListeners = new ArrayList<>();
				eventToListeners.put(eventType, eventListeners);
			}
			
			if (!eventListeners.contains(eventListener))
				eventListeners.add(eventListener);
		} else {			
			real.addEventListener(eventType, eventListener);
		}
	}

	@Override
	public synchronized <T> boolean removeEventListener(Class<T> eventType, IEventListener<T> eventListener) {
		if (real == null) {
			List<IEventListener<?>> eventListeners = eventToListeners.get(eventType);
			if (eventListeners == null)
				return false;
			
			boolean removed = eventListeners.remove(eventListener);
			if (eventListeners.isEmpty())
				eventToListeners.remove(eventType);
			
			return removed;
		} else {			
			return real.removeEventListener(eventType, eventListener);
		}
	}

	@Override
	public synchronized void addNotificationListener(INotificationListener notificationListener) {
		if (real == null) {
			notificationListeners.add(notificationListener);
		} else {			
			real.addNotificationListener(notificationListener);
		}
	}

	@Override
	public synchronized boolean removeNotificationListener(INotificationListener notificationListener) {
		if (real == null) {
			return notificationListeners.remove(notificationListener);
		} else {			
			return real.removeNotificationListener(notificationListener);
		}
	}

}
