package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Date;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "node-confirmation-request")
public class NodeConfirmationRequest {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "node-confirmation-request");
	
	private String concentratorThingName;
	private String nodeThingId;
	private String communicationNet;
	private Date requestedTime;
	
	public NodeConfirmationRequest() {}
	
	public NodeConfirmationRequest(String concentratorThingName, String nodeThingId,
				String communicationNet, Date requestedTime) {
		this.concentratorThingName = concentratorThingName;
		this.nodeThingId = nodeThingId;
		this.communicationNet = communicationNet;
		this.requestedTime = requestedTime;
	}
	
	public void setConcentratorThingName(String concentratorThingName) {
		this.concentratorThingName = concentratorThingName;
	}
	
	public void setNodeThingId(String nodeThingId) {
		this.nodeThingId = nodeThingId;
	}
	
	public void setCommunicationNet(String communicationNet) {
		this.communicationNet = communicationNet;
	}
	
	public void setRequestedTime(Date requestedTime) {
		this.requestedTime = requestedTime;
	}
	
	public String getConcentratorThingName() {
		return concentratorThingName;
	}

	public String getNodeThingId() {
		return nodeThingId;
	}

	public String getCommunicationNet() {
		return communicationNet;
	}

	public Date getRequestedTime() {
		return requestedTime;
	}
}
