package com.thefirstlinelinecode.sand.protocols.concentrator;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class ConcentratorDescriptor extends SimpleThingTypeDescriptor {
	public ConcentratorDescriptor() {
		super("concentrator", true, null, null, createSupportedActions());
	}

	private static Map<Protocol, Class<?>> createSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		supportedActions.put(ResetNode.PROTOCOL, ResetNode.class);
		supportedActions.put(SyncNodes.PROTOCOL, SyncNodes.class);
		
		return supportedActions;
	}
}
