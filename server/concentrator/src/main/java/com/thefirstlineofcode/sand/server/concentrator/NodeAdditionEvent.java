package com.thefirstlineofcode.sand.server.concentrator;

import java.util.Date;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;

public class NodeAdditionEvent implements IEvent  {
	private String requestId;
	private String concentratorThingName;
	private String nodeThingId;
	private Integer lanId;
	private String model;
	private Date additionTime;
	
	public NodeAdditionEvent(String requestId, String concentratorThingName, String nodeThingId,
			Integer lanId, String model, Date additionTime) {
		this.requestId = requestId;
		this.concentratorThingName = concentratorThingName;
		this.nodeThingId = nodeThingId;
		this.lanId = lanId;
		this.model = model;
		this.additionTime = additionTime;
	}
	
	public String getRequestId() {
		return requestId;
	}
	
	public String getConcentratorThingName() {
		return concentratorThingName;
	}
	
	public String getNodeThingId() {
		return nodeThingId;
	}
	
	public Integer getLanId() {
		return lanId;
	}
	
	public String getModel() {
		return model;
	}
	
	public Date getAdditionTime() {
		return additionTime;
	}

	@Override
	public Object clone() {
		return new NodeAdditionEvent(requestId, concentratorThingName, nodeThingId, lanId, model, additionTime);
	}
}
