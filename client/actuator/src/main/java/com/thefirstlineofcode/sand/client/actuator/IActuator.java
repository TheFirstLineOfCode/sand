package com.thefirstlineofcode.sand.client.actuator;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public interface IActuator {
	<T> void registerExecutor(Protocol protocol, Class<T> actionType, Class<? extends IExecutor<T>> executorType);
	<T> void registerExecutorFactory(IExecutorFactory<T> executorFactory);
	boolean isExecutorRegistered(Class<?> actionType);
	boolean unregisterExecutor(Class<?> actionType);
	void start();
	void stop();
	boolean isStarted();
	boolean isStopped();
}
