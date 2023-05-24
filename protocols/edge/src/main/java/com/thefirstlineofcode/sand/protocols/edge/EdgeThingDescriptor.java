package com.thefirstlineofcode.sand.protocols.edge;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class EdgeThingDescriptor extends SimpleThingTypeDescriptor {
	private static final String TYPE_NAME = "edge-thing";

	public EdgeThingDescriptor() {
		super(TYPE_NAME, false, null, null, createSupportedActions());
	}
	
	private static Map<Protocol, Class<?>> createSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		supportedActions.put(Stop.PROTOCOL, Stop.class);
		supportedActions.put(Restart.PROTOCOL, Restart.class);
		supportedActions.put(ShutdownSystem.PROTOCOL, ShutdownSystem.class);
		
		return supportedActions;
	}
}
