package com.thefirstlineofcode.sand.server.concentrator;

import java.util.Date;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;

public class NodeConfirmationRequestEvent implements IEvent {
	private String concentratorThingName;
	private String nodeThingId;
	private String communicationNet;
	private Date requestedTime;
	
	public NodeConfirmationRequestEvent(String concentratorThingName, String nodeThingId,
				String communicationNet, Date requestedTime) {
		this.concentratorThingName = concentratorThingName;
		this.nodeThingId = nodeThingId;
		this.communicationNet = communicationNet;
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
	
	public Object clone() {
		return new NodeConfirmationRequestEvent(concentratorThingName, nodeThingId, communicationNet, requestedTime);
	}
}
