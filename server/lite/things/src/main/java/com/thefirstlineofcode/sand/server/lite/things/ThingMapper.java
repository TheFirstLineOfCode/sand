package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.sand.server.things.Thing;

public interface ThingMapper {
	void insert(Thing thing);
	void delete(String thingId);
	Thing selectByThingId(String thingId);
	int selectCountByThingId(String thingId);
}
