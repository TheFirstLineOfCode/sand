package com.thefirstlineofcode.sand.client.thing;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;

public interface IEventProcessor<T> {
	void processEvent(JabberId notifier, T event);
}
