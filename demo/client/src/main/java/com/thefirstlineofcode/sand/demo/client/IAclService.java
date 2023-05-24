package com.thefirstlineofcode.sand.demo.client;

import java.util.List;

import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;

public interface IAclService {
	void retrieve();
	void retrieve(String thingId);
	void retrieve(int timeout);
	void retrieve(String thingId, int timeout);
	void delete(AccessControlEntry entry);
	void update(AccessControlEntry entry);
	void addListener(Listener listener);
	boolean removeListener(Listener listener);
	List<Listener> getListeners();
	void setLocal(AccessControlList local);
	AccessControlList getLocal();
	
	public interface Listener {
		void retrived(AccessControlList acl);
		void updated(AccessControlList acl);
		void timeout();
		void occurred(AclError error);
	}
}
