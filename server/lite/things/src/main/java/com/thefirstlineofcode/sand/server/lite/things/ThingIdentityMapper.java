package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

public interface ThingIdentityMapper {
	void insert(ThingIdentity identity);
	ThingIdentity selectByThingName(String thingName);
	String selectThingIdByThingName(String thingName);
	ThingIdentity selectByThingId(String thingId);
	int selectCountByThingName(String thingName);
}
