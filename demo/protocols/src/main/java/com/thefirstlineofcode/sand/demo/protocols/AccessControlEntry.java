package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.String2Enum;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.HandyUtils;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;

public class AccessControlEntry {
	@NotNull
	private String user;
	@NotNull
	private String thingId;
	@NotNull
	@String2Enum(Role.class)
	private Role role;
	private Boolean remove;
	
	public AccessControlEntry() {}
	
	public AccessControlEntry(String user, String thingId, Role role) {
		this.user = user;
		this.thingId = thingId;
		this.role = role;
		this.remove = null;
	}
	
	public String getThingId() {
		return thingId;
	}
	
	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public Boolean isRemove() {
		return remove != null && remove;
	}

	public void setRemove(Boolean remove) {
		this.remove = remove;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		
		if (user != null)
			hash += 31 * hash + user.hashCode();
		
		if (thingId != null)
			hash += 31 * hash + thingId.hashCode();
		
		if (role != null)
			hash += 31 * hash + role.hashCode();
		
		if (remove != null)
			hash += 31 * hash + remove.hashCode();
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccessControlEntry) {
			AccessControlEntry other = (AccessControlEntry)obj;
			if (!this.user.equals(other.user))
				return false;
			
			if (!HandyUtils.equalsEvenNull(this.thingId, other.thingId))
				return false;
			
			if (!this.role.equals(other.role))
				return false;
			
			return true;
		} else {
			return super.equals(obj);
		}
	}
	
}
