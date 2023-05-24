package com.thefirstlineofcode.sand.server.ibtr;

import org.pf4j.ExtensionPoint;

import com.thefirstlineofcode.sand.server.things.ThingRegistered;

public interface IThingRegistrationCustomizer extends ExtensionPoint {
	void registered(ThingRegistered registered);
	void tryToRegisterWithoutAuthorization(String thingId);
}
