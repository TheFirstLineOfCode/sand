package com.thefirstlineofcode.sand.client.actuator;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public abstract class ExecutorFactoryAdapter<T> implements IExecutorFactory<T> {
	@Override
	public Class<?> getActionResultType() {
		return null;
	}

	@Override
	public Protocol getActionResultProtocol() {
		return null;
	}
}
