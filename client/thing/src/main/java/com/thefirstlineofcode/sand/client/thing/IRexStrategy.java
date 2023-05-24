package com.thefirstlineofcode.sand.client.thing;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;

public interface IRexStrategy {
	void addRexListener(IRexListener rexListener);
	boolean removeRexListener(IRexListener rexListener);
	void addAckListener(IAckListener ackListener);
	boolean removeAckListener(IAckListener ackListener);
	void waitAck(IChatServices chatServices, Iq iq);
}
