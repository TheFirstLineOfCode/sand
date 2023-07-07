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
	@Text
	private String registrationCode;
	
	public Introduction() {}
	
	public Introduction(byte[] address, String thingId, String registrationCode) {
		this.address = address;
		this.thingId = thingId;
		this.registrationCode = registrationCode;
	}
	
	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}

	public String getRegistrationCode() {
		return registrationCode;
	}

	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}

	public byte[] getAddress() {
		return address;
	}
	
	public void setAddress(byte[] address) {
		this.address = address;
	}
}
