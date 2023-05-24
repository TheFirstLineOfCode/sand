package com.thefirstlineofcode.sand.demo.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;

public class AclService implements IAclService, IIqListener {
	private static final int DEFAULT_RETRIEVE_TIMEOUT = 5 * 1000;

	private List<Listener> listeners;
	private IChatServices chatServices;
	private AccessControlList local;
	
	public AclService(IChatServices chatServices) {
		this.chatServices = chatServices;
		listeners = new ArrayList<Listener>();
		
		chatServices.getIqService().addListener(AccessControlList.PROTOCOL, this);
	}

	@Override
	public void retrieve() {
		retrieve(DEFAULT_RETRIEVE_TIMEOUT);
	}
	
	@Override
	public void retrieve(final int timeout) {
		retrieve(null, timeout);
	}
	
	@Override
	public void retrieve(String thingId) {
		retrieve(thingId, DEFAULT_RETRIEVE_TIMEOUT);
	}

	@Override
	public void retrieve(final String thingId, final int timeout) {
		chatServices.getTaskService().execute(new ITask<Iq>() {
			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Iq iq = new Iq(Iq.Type.GET);
				iq.setObject(new AccessControlList());
				
				stream.send(iq, timeout);
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
				processRetrived(iq);
			}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				notifyError(new AclError(AclError.Type.SERVER_RETURNS_AN_ERROR, error));
				return true;
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (Listener listener : listeners) {
					listener.timeout();
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
		});
	}
	
	private void notifyError(AclError error) {
		for (Listener listener : listeners) {
			listener.occurred(error);
		}
	}
	
	private void processRetrived(Iq iq) {
		if (iq.getType() != Iq.Type.RESULT) {
			notifyError(new AclError(AclError.Type.INVALID_UPDATE_REQUEST));
			
			return;
		}
		
		AccessControlList acl = iq.getObject();
		local = acl;
		
		for (Listener listener : listeners) {
			listener.retrived(acl);
		}
	}

	private void updateAcl(AccessControlList acl) {
		if (!acl.getEntries().isEmpty()) {
			String thingId = acl.getThingId();
			for (AccessControlEntry ace : acl.getEntries()) {
				if (thingId != null && ace.getThingId() == null) {
					ace.setThingId(thingId);
				}
				
				if (ace.getUser() == null) {
					ace.setUser(chatServices.getStream().getJid().getNode());
				}
			}
		}
		
		if (local == null) {
			local = acl;
			notifyUpdated(acl);
		} else {
			AccessControlList updated = new AccessControlList();
			for (AccessControlEntry ace : acl.getEntries()) {
				if (addOrUpdateToLocal(ace))
					updated.add(ace);
			}
			
			notifyUpdated(updated);
		}
	}

	private void notifyUpdated(AccessControlList updated) {
		for (Listener listener : listeners) {
			listener.updated(updated);
		}
	}

	private boolean addOrUpdateToLocal(AccessControlEntry ace) {
		if (!local.contains(ace)) {
			local.add(ace);
			return true;
		} else {
			return local.update(ace);
		}
	}

	@Override
	public void addListener(Listener listener) {
		if (listeners.contains(listener))
			this.listeners.add(listener);
	}

	@Override
	public boolean removeListener(Listener listener) {
		return listeners.remove(listener);
	}

	@Override
	public void delete(AccessControlEntry entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(AccessControlEntry entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Listener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}
	
	@Override
	public void setLocal(AccessControlList local) {
		this.local = local;
	}

	@Override
	public AccessControlList getLocal() {
		return local;
	}

	@Override
	public void received(Iq iq) {
		if (iq.getType() != Iq.Type.SET) {
			notifyError(new AclError(AclError.Type.INVALID_UPDATE_REQUEST));
			
			return;
		}
		
		updateAcl((AccessControlList)iq.getObject());
	}

}
