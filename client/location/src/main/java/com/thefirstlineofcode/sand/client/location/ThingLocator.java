package com.thefirstlineofcode.sand.client.location;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.sand.protocols.location.LocateThings;

public class ThingLocator implements IThingLocator {
	private IChatServices chatServices;
	
	private List<Listener> listeners;
	
	public ThingLocator() {
		listeners = new ArrayList<>();
	}
	
	@Override
	public void locateThings(final List<String> thingIds) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				LocateThings locateThings = new LocateThings();
				locateThings.setThingIds(thingIds);
				
				Iq iq = new Iq(Iq.Type.GET, locateThings, Stanza.generateId("lct"));
				stream.send(iq);
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
				LocateThings locateThings = iq.getObject();
				for (Listener listener : listeners) {
					listener.located(locateThings.getThingLocations());
				}
			}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				for (Listener listener : listeners) {
					listener.occurred(error);
				}
				
				return true;
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
				for (Listener listener : listeners) {
					listener.timeout();
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
			
		});
	}

	@Override
	public void addListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public boolean removeListener(Listener listener) {
		return listeners.remove(listener);
	}

}
