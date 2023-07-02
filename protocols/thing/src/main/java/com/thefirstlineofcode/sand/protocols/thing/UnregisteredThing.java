package com.thefirstlineofcode.sand.protocols.thing;

public class UnregisteredThing {
	private String thingId;
	private String registrationKey;
	
	public UnregisteredThing() {}
	
	public UnregisteredThing(String thingId, String registrationKey) {
		this.thingId = thingId;
		this.registrationKey = registrationKey;
	}
	
	public String getThingId() {
		return thingId;
	}
	
	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public String getRegistrationKey() {
		return registrationKey;
	}
	
	public void setRegistrationKey(String registrationKey) {
		this.registrationKey = registrationKey;
	}
}
