package com.thefirstlineofcode.sand.protocols.lora.dac;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class LoraDacServiceDescriptor extends SimpleThingTypeDescriptor {
	public LoraDacServiceDescriptor() {
		super("lora-dac", true, createSupportedEvents(), null, createSupportedActions());
	}

	private static Map<Protocol, Class<?>> createSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		supportedActions.put(ResetLoraDacService.PROTOCOL, ResetLoraDacService.class);
		
		return supportedActions;
	}

	private static Map<Protocol, Class<?>> createSupportedEvents() {
		Map<Protocol, Class<?>> supportedEvents = new HashMap<>();
		supportedEvents.put(Reconfigure.PROTOCOL, Reconfigure.class);
		
		return supportedEvents;
	}
}
