package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.Text;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:lora-dac", localName="reconfigure")
public class Reconfigure {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:lora-dac", "reconfigure");
	
	@Text
	private String thingId;
	
	public Reconfigure() {}
	
	public Reconfigure(String thingId) {
		this.thingId = thingId;
	}
	
	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
}
