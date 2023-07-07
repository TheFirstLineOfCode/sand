package com.thefirstlineofcode.sand.server.ibtr;

import com.thefirstlineofcode.sand.server.things.ThingRegistered;

public interface IThingRegistrar {
	ThingRegistered register(String thingId, String registrationCode);
	void remove(String thingId);
}
