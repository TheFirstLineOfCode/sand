package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;

public interface RegisteredThingMapper {
	void insert(RegisteredThing registeredThing);
	RegisteredThing selectByThingName(String thingName);
	String selectThingIdByThingName(String thingName);
	RegisteredThing selectByThingId(String thingId);
	int selectCountByThingName(String thingName);
}
