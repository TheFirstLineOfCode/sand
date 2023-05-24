package com.thefirstlineofcode.sand.server.things;

public interface IThingIdRuler {
	boolean isValid(String thingId);
	String guessModel(String thingId);
}
