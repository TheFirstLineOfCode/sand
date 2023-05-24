package com.thefirstlineofcode.sand.protocols.thing;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public interface IThingDescriptor {
	public boolean isActuator();	
	public boolean isConcentrator();
	public boolean isSensor();
	
	public Map<Protocol, Class<?>> getSupportedEvents();
	public Map<Protocol, Class<?>> getFollowedEvents();
	public Map<Protocol, Class<?>> getSupportedData();
	public Map<Protocol, Class<?>> getSupportedActions();
}
