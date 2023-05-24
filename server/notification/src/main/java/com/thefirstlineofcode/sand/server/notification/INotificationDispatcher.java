package com.thefirstlineofcode.sand.server.notification;


public interface INotificationDispatcher {
	public static final String NAME_APP_COMPONENT_NOTIFICATION_DISPATCHER = "notification.dispatcher";
	
	<T> void addEventListener(Class<T> eventType, IEventListener<T> eventListener);
	<T> boolean removeEventListener(Class<T> eventType, IEventListener<T> eventListener);
	void addNotificationListener(INotificationListener notificationListener); 
	boolean removeNotificationListener(INotificationListener notificationListener);
}
