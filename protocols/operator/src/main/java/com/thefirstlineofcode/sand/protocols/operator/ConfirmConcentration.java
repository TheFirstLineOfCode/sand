package com.thefirstlineofcode.sand.protocols.operator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "urn:leps:tuxp:operator", localName = "confirm-concentration")
public class ConfirmConcentration {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:operator", "confirm-concentration");
	
	private String concentratorThingName;
	private String nodeThingId;
	
	public ConfirmConcentration() {}
	
	public ConfirmConcentration(String concentratorThingName, String nodeThingId) {
		this.concentratorThingName = concentratorThingName;
		this.nodeThingId = nodeThingId;
	}
	
	public String getConcentratorThingName() {
		return concentratorThingName;
	}
	
	public void setConcentratorThingName(String concentratorThingName) {
		this.concentratorThingName = concentratorThingName;
	}
	
	public String getNodeThingId() {
		return nodeThingId;
	}
	
	public void setNodeThingId(String nodeThingId) {
		this.nodeThingId = nodeThingId;
	}
}
