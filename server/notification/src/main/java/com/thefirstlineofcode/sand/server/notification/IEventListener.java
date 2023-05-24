package com.thefirstlineofcode.sand.server.notification;


import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;

public interface IEventListener<T> {
	void eventReceived(IProcessingContext context, JabberId notifier, T event);
}
