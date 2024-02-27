package com.thefirstlineofcode.sand.server.things;

public interface IEdgeThingManager {
	EdgeThingRegistered register(String thingId, String registrationCode);
	Thing getByThingName(String thingName);
	boolean thingNameExists(String thingName);
	String getThingNameByThingId(String thingId);
	String getThingIdByThingName(String thingName);
}
