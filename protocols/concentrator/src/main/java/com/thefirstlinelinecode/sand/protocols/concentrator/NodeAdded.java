package com.thefirstlinelinecode.sand.protocols.concentrator;

import java.util.Date;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:concentrator", localName="node-added")
public class NodeAdded {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:concentrator", "node-added");
	
	@NotNull
	private String concentratorThingName;
	@NotNull
	private String nodeThingId;
	@NotNull
	private Integer lanId;
	@NotNull
	private String model;
	@NotNull
	private Date additionTime;
	
	public NodeAdded() {}
	
	public NodeAdded(String concentratorThingName, String nodeThingId,
			Integer lanId, String model, Date additionTime) {
		this.concentratorThingName = concentratorThingName;
		this.nodeThingId = nodeThingId;
		this.lanId = lanId;
		this.model = model;
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
	
	public String getModel() {
		return model;
	}
	
	public void setModel(String model) {
		this.model = model;
	}

	public Date getAdditionTime() {
		return additionTime;
	}

	public void setAdditionTime(Date additionTime) {
		this.additionTime = additionTime;
	}
	
}
