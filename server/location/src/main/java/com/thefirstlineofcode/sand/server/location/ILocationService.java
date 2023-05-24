package com.thefirstlineofcode.sand.server.location;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.sand.protocols.location.ThingLocation;

public interface ILocationService {
	List<ThingLocation> locateThings(List<String> thingIds);
	String getThingIdByLocation(String location);
	String getThingIdByJid(JabberId jid);
	String getLocationByThingId(String thingId);
}
