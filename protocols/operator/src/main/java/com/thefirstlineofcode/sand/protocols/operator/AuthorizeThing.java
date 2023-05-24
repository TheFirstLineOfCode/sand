package com.thefirstlineofcode.sand.protocols.operator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "urn:leps:tacp:operator", localName = "auth-thing")
public class AuthorizeThing {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:operator", "auth-thing");
	
	@NotNull
	private String thingId;
	private boolean canceled;
	
	public AuthorizeThing() {}
	
	public AuthorizeThing(String thingId) {
		this(thingId, false);
	}
	
	public AuthorizeThing(String thingId, boolean canceled) {
		this.thingId = thingId;
		this.canceled = canceled;
	}
	
	public String getThingId() {
		return thingId;
	}
	
	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
	
}
