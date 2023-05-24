package com.thefirstlineofcode.sand.client.friends;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;

public interface IFollowProcessor {
	void process(JabberId friend, Object event);
}
