package com.thefirstlineofcode.sand.demo.protocols;

public class LanNode {
	private String thingId;
	private Integer lanId;
	private String model;
	private String communicationNet;
	
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
}
