package com.thefirstlineofcode.sand.server.things;

import java.util.Date;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;

public class Thing implements IIdProvider<String> {
	private String id;
	private String thingId;
	private String registrationCode;
	private String model;
	private String softwareVersion;
	private Date registrationTime;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public String getRegistrationCode() {
		return registrationCode;
	}
	
	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}

	public String getModel() {
		return model;
	}
	
	public void setModel(String model) {
		this.model = model;
	}
	
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}
	
	public Date getRegistrationTime() {
		return registrationTime;
	}
	
	public void setRegistrationTime(Date registrationTime) {
		this.registrationTime = registrationTime;
	}
	
}
