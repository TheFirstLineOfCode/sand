package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.Text;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:lora-dac", localName="introduction")
public class Introduction {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:lora-dac", "introduction");
	
	private byte[] address;
	@Text
	private String thingId;
	
	public Introduction() {}
	
	public Introduction(byte[] address, String thingId) {
		this.address = address;
		this.thingId = thingId;
	}
	
	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}

	public byte[] getAddress() {
		return address;
	}
	
	public void setAddress(byte[] address) {
		this.address = address;
	}
}
