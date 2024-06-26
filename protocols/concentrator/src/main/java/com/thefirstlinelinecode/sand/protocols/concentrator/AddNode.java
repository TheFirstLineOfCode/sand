package com.thefirstlinelinecode.sand.protocols.concentrator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.IntRange;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:concentrator", localName="add-node")
public class AddNode {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:concentrator", "add-node");
	
	@NotNull
	private String thingId;
	@NotNull
	private String registrationCode;
	@NotNull
	@IntRange(min = 1, max = 255)
	private Integer lanId;
	@NotNull
	private String communicationNet;
	@NotNull
	private String address;
	
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

	public Integer getLanId() {
		return lanId;
	}
	
	public void setLanId(Integer lanId) {
		this.lanId = lanId;
	}

	public String getCommunicationNet() {
		return communicationNet;
	}

	public void setCommunicationNet(String communicationNet) {
		this.communicationNet = communicationNet;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
}
