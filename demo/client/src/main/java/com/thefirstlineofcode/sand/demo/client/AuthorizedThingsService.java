package com.thefirstlineofcode.sand.demo.client;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedThings;

public class AuthorizedThingsService implements IAuthorizedThingsService {
	private IChatServices chatServices;
	
	private List<Listener> listeners;
	
	public AuthorizedThingsService() {
		listeners = new ArrayList<Listener>();
	}

	@Override
	public void retrieve() {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				stream.send(new Iq(Iq.Type.GET, new AuthorizedThings(), Stanza.generateId("ads")));
			}
			
			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (Listener listener : listeners) {
					listener.retrieved((AuthorizedThings)iq.getObject());
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
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (Listener listener : listeners) {
					listener.timeout();;
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
			this.listeners.add(listener);
	}

	@Override
	public boolean removeListener(Listener listener) {
		return listeners.remove(listener);
	}

}
