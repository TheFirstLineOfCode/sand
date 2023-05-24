package com.thefirstlineofcode.sand.protocols.thing;

import java.io.Serializable;

public class ThingIdentity implements Serializable {
	private static final long serialVersionUID = 2975514130104649088L;
	
	public static final String DEFAULT_RESOURCE_NAME = "0";
	
	private String thingName;
	private String credentials;
	
	public ThingIdentity() {}
	
	public ThingIdentity(String thingName, String credentials) {
		this.thingName = thingName;
		this.credentials = credentials;
	}

	public void setThingName(String thingName) {
		this.thingName = thingName;
	}
	
	public String getThingName() {
		return thingName;
	}
	
	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public String getCredentials() {
		return credentials;
	}
	
}
