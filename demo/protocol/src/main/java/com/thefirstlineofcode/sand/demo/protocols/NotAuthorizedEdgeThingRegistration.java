package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.Text;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "not-authorized-edge-thing-registration")
public class NotAuthorizedEdgeThingRegistration {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "not-authorized-edge-thing-registration");
	
	@Text
	private String thingId;
	
	public NotAuthorizedEdgeThingRegistration() {}
	
	public NotAuthorizedEdgeThingRegistration(String thingId) {
		this.thingId = thingId;
	}

	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	
}
