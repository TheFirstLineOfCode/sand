package com.thefirstlineofcode.sand.protocols.thing;

import java.io.Serializable;

public class RegisteredThing implements Serializable {
	private static final long serialVersionUID = 2975514130104649088L;
	
	public static final String DEFAULT_RESOURCE_NAME = "0";
	
	private String thingName;
	private String credentials;
	private byte[] secretKey;
	
	public RegisteredThing() {}
	
	public RegisteredThing(String thingName, String credentials, byte[] secretKey) {
		if (thingName == null)
			throw new IllegalArgumentException("Null thing name.");
		
		if (credentials == null)
			throw new IllegalArgumentException("Null credentials.");
		
		if (secretKey == null)
			throw new IllegalArgumentException("Null secret key.");
		
		this.thingName = thingName;
		this.credentials = credentials;
		this.secretKey = secretKey;
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

	public byte[] getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(byte[] securityKey) {
		this.secretKey = securityKey;
	}
}
