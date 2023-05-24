package com.thefirstlineofcode.sand.client.thing;

public interface INotificationService {
	<T> void listen(Class<T> eventType, IEventProcessor<T> eventProcessor);
	INotifier getNotifier();
}
