package com.thefirstlineofcode.sand.client.actuator;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public interface IExecutorFactory<T> {
	Protocol getProtocol();
	Class<T> getActionType();
	Class<?> getActionResultType();
	Protocol getActionResultProtocol();
	IExecutor<T> create();
}
