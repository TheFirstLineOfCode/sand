package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Date;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "node-addition")
public class NodeAddition {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "node-addition");
	
	private String concentratorThingName;
	private String nodeThingId;
	private Integer lanId;
	private Date additionTime;
	
	public NodeAddition() {}
	
	public NodeAddition(String concentratorThingName, String nodeThingId, Integer lanId, Date additionTime) {
		this.concentratorThingName = concentratorThingName;
		this.nodeThingId = nodeThingId;
		this.lanId = lanId;
		this.additionTime = additionTime;
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

	public Date getAdditionTime() {
		return additionTime;
	}

	public void setAdditionTime(Date additionTime) {
		this.additionTime = additionTime;
	}
	
}
