package com.thefirstlineofcode.sand.demo.protocols;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.HandyUtils;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "acl")
public class AccessControlList {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "acl");
	
	public enum Role {
		OWNER,
		VIEWER,
		CONTROLLER
	}
	
	private String thingId;
	
	@Array(AccessControlEntry.class)
	private List<AccessControlEntry> entries;
	
	public AccessControlList() {
		entries = new ArrayList<AccessControlEntry>();
	}

	public List<AccessControlEntry> getEntries() {
		return entries;
	}
	
	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}

	public void setEntries(List<AccessControlEntry> entries) {
		this.entries = entries;
	}
	
	public boolean contains(AccessControlEntry entry) {
		if (entry.getUser() == null)
			throw new RuntimeException("Null user.");
		
		if (entry.getThingId() == null)
			throw new RuntimeException("Null thing ID.");
			
		if (entries == null || entries.isEmpty())
			return false;
		
		for (AccessControlEntry anEntry : entries) {
			if (anEntry.getThingId().equals(entry.getThingId()) &&
					HandyUtils.equalsEvenNull(anEntry.getUser(), entry.getUser()))
				return true;
		}
		
		return false;
	}
	
	public void add(AccessControlEntry entry) {
		if (contains(entry))
			throw new RuntimeException("Entry has existed.");
		
		entries.add(entry);
	}

	public boolean update(AccessControlEntry entry) {
		for (AccessControlEntry anEntry : entries) {
			if (anEntry.getThingId().equals(entry.getThingId()) &&
					anEntry.getUser().equals(entry.getUser()))
				anEntry.setRole(entry.getRole());
			
			return true;
		}
		
		throw new RuntimeException(String.format("Entry[%s, %s] doesn't exist.", entry.getThingId(), entry.getUser()));
	}
}
