package com.thefirstlineofcode.sand.server.things;

import java.util.Date;

import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

public class ThingRegistered {
	public String thingId;
	public ThingIdentity thingIdentity;
	public String authorizer;
	public Date registrationTime;
	
	public ThingRegistered(String thingId, ThingIdentity thingIdentity,
			String authorizer, Date registrationTime) {
		this.thingId = thingId;
		this.thingIdentity = thingIdentity;
		this.authorizer = authorizer;
		this.registrationTime = registrationTime;
	}
}
