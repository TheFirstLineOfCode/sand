package com.thefirstlineofcode.sand.server.concentrator;

public interface IConcentratorFactory {
	boolean isConcentrator(String thingId);
	IConcentrator getConcentrator(String thingId);
	String getConcentratorThingNameByNodeThingId(String nodeThingId);
	boolean isLanNode(String thingId);
}
