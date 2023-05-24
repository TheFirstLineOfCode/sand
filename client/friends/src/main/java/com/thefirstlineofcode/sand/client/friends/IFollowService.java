package com.thefirstlineofcode.sand.client.friends;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public interface IFollowService {
	void registerFollowedEvent(Protocol protocol, Class<?> eventType);
	void setFollowProcessor(IFollowProcessor followProcessor);
	void start();
	void stop();
	boolean isStarted();
}
