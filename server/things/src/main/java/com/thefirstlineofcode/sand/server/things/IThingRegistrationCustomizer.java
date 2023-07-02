package com.thefirstlineofcode.sand.server.things;

import org.pf4j.ExtensionPoint;

public interface IThingRegistrationCustomizer extends ExtensionPoint {
	boolean isUnregisteredThing(String thingId, String registrationKey);
	String guessModel(String thingId);
	boolean isAuthenticationRequired();
	void setThingManager(IThingManager thingManager);
	byte[] createSecurityKey();
}
