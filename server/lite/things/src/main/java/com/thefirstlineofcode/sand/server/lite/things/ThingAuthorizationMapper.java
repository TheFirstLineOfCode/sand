package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.sand.server.things.ThingAuthorization;

public interface ThingAuthorizationMapper {
	void insert(ThingAuthorization authorization);
	void updateCanceled(String thingId, boolean canceled);
	ThingAuthorization[] selectByThingId(String thingId);
}
