package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;

public interface IAclService {
	void add(AccessControlEntry ace);
	void change(AccessControlEntry ace);
	void remove(String user, String thingId);
	AccessControlList getUserAcl(String user);
	AccessControlList getOwnerAcl(String thingId);
	Role getRole(String user, String thingId);
	String getOwner(String thingId);
	boolean isOwner(String user, String thingId);
}
