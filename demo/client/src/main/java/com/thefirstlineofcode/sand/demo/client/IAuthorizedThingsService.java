package com.thefirstlineofcode.sand.demo.client;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedThings;

public interface IAuthorizedThingsService {
	void retrieve();
	void addListener(Listener listener);
	boolean removeListener(Listener listener);
	
	public interface Listener {
		void retrieved(AuthorizedThings things);
		void timeout();
		void occurred(StanzaError error);
	}
}
