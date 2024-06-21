package com.thefirstlineofcode.sand.client.actuator;

public interface IActuator {
	<T> void registerExecutor(Class<T> actionType, Class<? extends IExecutor<T>> executorType);
	<T> void registerExecutor(Class<T> actionType, Class<?> actionResultType, Class<? extends IExecutor<T>> executorType);
	<T> void registerExecutor(Class<T> actionType, Class<? extends IExecutor<T>> executorType, Object thingController);
	<T> void registerExecutor(Class<T> actionType, Class<?> actionResultType, Class<? extends IExecutor<T>> executorType, Object thingController);
	<T> void registerExecutorFactory(IExecutorFactory<T> executorFactory);
	boolean isExecutorRegistered(Class<?> actionType);
	boolean unregisterExecutor(Class<?> actionType);
	void start();
	void stop();
	boolean isStarted();
	boolean isStopped();
}
