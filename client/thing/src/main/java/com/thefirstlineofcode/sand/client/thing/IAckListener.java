package com.thefirstlineofcode.sand.client.thing;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;

public interface IAckListener {
	void acked(Iq iq);
	void noAck(Iq iq);
}
