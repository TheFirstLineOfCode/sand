package com.thefirstlineofcode.sand.demo.server.lite;

import java.util.List;

import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;

public interface AccessControlEntryMapper {
	void insert(AccessControlEntry ace);
	void remove(String user, String thingId);
	void updateRole(String user, String thingId, Role role);
	List<AccessControlEntry> selectByUser(String user);
	List<AccessControlEntry> selectByThingId(String thingId);
	String selectOwnerByThingId(String thingId);
	Role selectRoleByUserAndThingId(String user, String thingId);
}
