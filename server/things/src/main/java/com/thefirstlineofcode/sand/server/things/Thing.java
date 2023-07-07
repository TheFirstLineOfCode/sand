package com.thefirstlineofcode.sand.server.things;

import java.util.Date;

public class Thing {
	private String thingId;
	private String registrationCode;
	private String model;
	private String softwareVersion;
	private Date registrationTime;
	
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
