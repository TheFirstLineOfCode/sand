package com.thefirstlineofcode.sand.protocols.thing;

import java.io.Serializable;

public class RegisteredThing implements Serializable {
	private static final long serialVersionUID = 2975514130104649088L;
	
	public static final String DEFAULT_RESOURCE_NAME = "0";
	
	private String thingName;
	private String credentials;
	private byte[] securityKey;
	
	public RegisteredThing() {}
	
	public RegisteredThing(String thingName, String credentials, byte[] securityKey) {
		if (thingName == null)
			throw new IllegalArgumentException("Null thing name.");
		
		if (credentials == null)
			throw new IllegalArgumentException("Null credentials.");
		
		if (securityKey == null)
			throw new IllegalArgumentException("Null security key.");
		
		this.thingName = thingName;
		this.credentials = credentials;
		this.securityKey = securityKey;
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

	public byte[] getSecurityKey() {
		return securityKey;
	}

	public void setSecurityKey(byte[] securityKey) {
		this.securityKey = securityKey;
	}
}
