package com.thefirstlineofcode.sand.server.things;

import org.pf4j.ExtensionPoint;

public interface IThingRegistrationCustomizer extends ExtensionPoint {
	boolean isUnregisteredThing(String thingId, String registrationCode);
	String guessModel(String thingId);
	boolean isAuthorizationRequired();
	boolean isConfirmationRequired();
	void setThingManager(IThingManager thingManager);
	byte[] createSecurityKey();
}
