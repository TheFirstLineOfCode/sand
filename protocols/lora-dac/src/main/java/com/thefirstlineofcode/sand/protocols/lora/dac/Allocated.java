package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.Text;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:lora-dac", localName="allocated")
public class Allocated {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:lora-dac", "allocated");
	
	@Text
	private String thingId;
	
	public Allocated() {}
	
	public Allocated(String thingId) {
		this.thingId = thingId;
	}
	
	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}	
}
