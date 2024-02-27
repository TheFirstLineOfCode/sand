package com.thefirstlineofcode.sand.server.things;

import java.util.Date;

import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;

public class EdgeThingRegistered {
	public String thingId;
	public RegisteredEdgeThing registeredEdgeThing;
	public String authorizer;
	public Date registrationTime;
	
	public EdgeThingRegistered(String thingId, RegisteredEdgeThing registeredEdgeThing,
			String authorizer, Date registrationTime) {
		this.thingId = thingId;
		this.registeredEdgeThing = registeredEdgeThing;
		this.authorizer = authorizer;
		this.registrationTime = registrationTime;
	}
}
