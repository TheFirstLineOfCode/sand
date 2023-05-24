package com.thefirstlinelinecode.sand.protocols.concentrator;

public class Node {
	private String thingId;
	private Integer lanId;
	private String model;
	private String communicationNet;
	private String address;
	
	public String getThingId() {
		return thingId;
	}
	
	public void setThingId(String node) {
		this.thingId = node;
	}
	
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
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
