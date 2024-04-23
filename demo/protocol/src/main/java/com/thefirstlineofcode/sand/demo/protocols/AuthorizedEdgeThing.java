package com.thefirstlineofcode.sand.demo.protocols;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.String2Enum;
import com.thefirstlineofcode.basalt.xmpp.HandyUtils;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;

public class AuthorizedEdgeThing {
	private String thingId;
	private String thingName;
	private String model;
	
	@String2Enum(Role.class)
	private Role role;
	
	private boolean concentrator;
	
	@Array(value = LanNode.class, elementName = "node")
	private List<LanNode> nodes;
	
	public AuthorizedEdgeThing() {
		concentrator = false;
		nodes = new ArrayList<>();
	}
	
	public String getThingId() {
		return thingId;
	}
	
	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public String getThingName() {
		return thingName;
	}
	
	public void setThingName(String thingName) {
		this.thingName = thingName;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public boolean isConcentrator() {
		return concentrator;
	}

	public void setConcentrator(boolean concentrator) {
		this.concentrator = concentrator;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<LanNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<LanNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		
		if (thingId != null)
			hash += 31 * hash + thingId.hashCode();
		
		if (thingName != null)
			hash += 31 * hash + thingName.hashCode();
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AuthorizedEdgeThing))
			return false;
		
		AuthorizedEdgeThing other = (AuthorizedEdgeThing)obj;
		
		return HandyUtils.equalsEvenNull(this.thingId, other.thingId) &&
				HandyUtils.equalsEvenNull(this.thingName, this.thingName);
	}
}
