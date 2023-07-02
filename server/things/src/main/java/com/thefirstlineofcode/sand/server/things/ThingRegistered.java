package com.thefirstlineofcode.sand.server.things;

import java.util.Date;

import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;

public class ThingRegistered {
	public String thingId;
	public RegisteredThing registeredThing;
	public String authorizer;
	public Date registrationTime;
	
	public ThingRegistered(String thingId, RegisteredThing registeredThing,
			String authorizer, Date registrationTime) {
		this.thingId = thingId;
		this.registeredThing = registeredThing;
		this.authorizer = authorizer;
		this.registrationTime = registrationTime;
	}
}
