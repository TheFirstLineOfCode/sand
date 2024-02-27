package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;

public interface RegisteredEdgeThingMapper {
	void insert(RegisteredEdgeThing registeredEdgeThing);
	RegisteredEdgeThing selectByThingName(String thingName);
	String selectThingIdByThingName(String thingName);
	RegisteredEdgeThing selectByThingId(String thingId);
	int selectCountByThingName(String thingName);
}
