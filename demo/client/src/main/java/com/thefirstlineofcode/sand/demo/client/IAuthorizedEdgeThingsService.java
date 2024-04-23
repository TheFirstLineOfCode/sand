package com.thefirstlineofcode.sand.demo.client;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedEdgeThings;

public interface IAuthorizedEdgeThingsService {
	void retrieve();
	void addListener(Listener listener);
	boolean removeListener(Listener listener);
	
	public interface Listener {
		void retrieved(AuthorizedEdgeThings things);
		void timeout();
		void occurred(StanzaError error);
	}
}
