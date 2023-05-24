package com.thefirstlineofcode.sand.client.location;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.sand.protocols.location.ThingLocation;

public interface IThingLocator {
	void locateThings(List<String> thingIds);
	void addListener(Listener listener);
	boolean removeListener(Listener listener);
	
	public interface Listener {
		void located(List<ThingLocation> thingLocations);
		void occurred(StanzaError error);
		void timeout();
	}
}
