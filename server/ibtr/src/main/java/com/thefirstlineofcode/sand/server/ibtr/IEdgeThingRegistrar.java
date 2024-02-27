package com.thefirstlineofcode.sand.server.ibtr;

import com.thefirstlineofcode.sand.server.things.EdgeThingRegistered;

public interface IEdgeThingRegistrar {
	EdgeThingRegistered register(String thingId, String registrationCode);
	void remove(String thingId);
}
