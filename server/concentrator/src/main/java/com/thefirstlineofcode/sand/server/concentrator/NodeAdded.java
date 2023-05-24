package com.thefirstlineofcode.sand.server.concentrator;

import java.util.Date;

public class NodeAdded {
	private String requestId;
	private String concentratorThingName;
	private String nodeThingId;
	private Integer lanId;
	private String model;
	private Date addedTime;
	private Date confirmedTime;
	
	public NodeAdded(String requestId, String concentratorThingName, String nodeThingId, Integer lanId,
			String model, Date addedTime, Date confirmedTime) {
		this.requestId = requestId;
		this.concentratorThingName = concentratorThingName;
		this.nodeThingId = nodeThingId;
		this.lanId = lanId;
		this.model = model;
		this.addedTime = addedTime;
		this.confirmedTime = confirmedTime;
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
	
	public Date getAddedTime() {
		return addedTime;
	}
	
	public Date getConfirmedTime() {
		return confirmedTime;
	}	
	
}
