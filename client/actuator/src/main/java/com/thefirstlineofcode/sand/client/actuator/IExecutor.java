package com.thefirstlineofcode.sand.client.actuator;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;

public interface IExecutor<T> {
	Object execute(Iq iq, T action) throws ProtocolException;
}
