package com.thefirstlineofcode.sand.client.thing;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;

public interface IRexListener {
	void retransmit(Iq iq);
	void abandon(Iq iq);
	void acked(Iq iq);
}
