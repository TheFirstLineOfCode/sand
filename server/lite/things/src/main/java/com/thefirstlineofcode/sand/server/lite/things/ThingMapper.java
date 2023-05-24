package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.sand.server.things.Thing;

public interface ThingMapper {
	void insert(Thing thing);
	void delete(String thingId);
	Thing selectByThingId(String thingId);
	Thing selectByThingName(String thingName);
	int selectCountByThingId(String thingId);
	int selectCountByThingName(String thingName);
}
