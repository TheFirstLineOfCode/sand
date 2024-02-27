package com.thefirstlineofcode.sand.protocols.thing;

public class UnregisteredEdgeThing {
	private String thingId;
	private String registrationCode;
	
	public UnregisteredEdgeThing() {}
	
	public UnregisteredEdgeThing(String thingId, String registrationCode) {
		this.thingId = thingId;
		this.registrationCode = registrationCode;
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
}
