package com.thefirstlineofcode.sand.protocols.thing.lora;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ResetThing;

public class LoraThingDescriptor extends SimpleThingTypeDescriptor {
	private static final String TYPE_NAME = "lora-thing";

	public LoraThingDescriptor() {
		super(TYPE_NAME, false, null, null, createSupportedActions());
	}
	
	private static Map<Protocol, Class<?>> createSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		supportedActions.put(ResetThing.PROTOCOL, ResetThing.class);
		
		return supportedActions;
	}
}
