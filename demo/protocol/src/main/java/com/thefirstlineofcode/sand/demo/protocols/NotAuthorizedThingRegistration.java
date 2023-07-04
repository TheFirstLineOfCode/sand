package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.Text;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "not-authorized-thing-registration")
public class NotAuthorizedThingRegistration {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "not-authorized-thing-registration");
	
	@Text
	private String thingId;
	
	public NotAuthorizedThingRegistration() {}
	
	public NotAuthorizedThingRegistration(String thingId) {
		this.thingId = thingId;
	}

	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	
}
