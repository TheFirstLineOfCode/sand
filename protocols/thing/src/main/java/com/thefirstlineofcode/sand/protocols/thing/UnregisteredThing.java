package com.thefirstlineofcode.sand.protocols.thing;

public class UnregisteredThing {
	private String thingId;
	private String registrationCode;
	
	public UnregisteredThing() {}
	
	public UnregisteredThing(String thingId, String registrationCode) {
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
