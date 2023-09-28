package com.thefirstlineofcode.sand.server.concentrator;

import java.util.Date;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;

public class Concentration implements IIdProvider<String> {
	private String id;
	private String concentratorThingName;
	private String nodeThingId;
	private Integer lanId;
	private String communicationNet;
	private String address;
	private Date additionTime;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
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
	
	public Date getAdditionTime() {
		return additionTime;
	}
	
	public void setAdditionTime(Date additionTime) {
		this.additionTime = additionTime;
	}
}
