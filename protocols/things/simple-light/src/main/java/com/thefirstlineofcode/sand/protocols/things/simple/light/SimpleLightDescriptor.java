package com.thefirstlineofcode.sand.protocols.things.simple.light;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class SimpleLightDescriptor extends SimpleThingTypeDescriptor {
	public static final String TYPE_NAME = "simple-light";
	
	public SimpleLightDescriptor() {
		super(TYPE_NAME, false, createSupportedEvents(), null, createSupportedActions());
	}
	
	private static Map<Protocol, Class<?>> createSupportedEvents() {
		Map<Protocol, Class<?>> supportedEvents = new HashMap<>();
		supportedEvents.put(SwitchStateChanged.PROTOCOL, SwitchStateChanged.class);
		
		return supportedEvents;
	}

	private static Map<Protocol, Class<?>> createSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		supportedActions.put(Flash.PROTOCOL, Flash.class);
		supportedActions.put(TurnOn.PROTOCOL, TurnOn.class);
		supportedActions.put(TurnOff.PROTOCOL, TurnOff.class);
		
		return supportedActions;
	}
}
