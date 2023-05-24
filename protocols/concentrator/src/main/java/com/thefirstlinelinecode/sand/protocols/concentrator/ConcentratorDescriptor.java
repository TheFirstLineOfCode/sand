package com.thefirstlinelinecode.sand.protocols.concentrator;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlinelinecode.sand.protocols.concentrator.friends.PullLanFollows;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class ConcentratorDescriptor extends SimpleThingTypeDescriptor {
	public ConcentratorDescriptor() {
		super("concentrator", true, createSupportedEvents(), null, createSupportedActions());
	}

	private static Map<Protocol, Class<?>> createSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		supportedActions.put(ResetNode.PROTOCOL, ResetNode.class);
		supportedActions.put(SyncNodes.PROTOCOL, SyncNodes.class);
		supportedActions.put(PullLanFollows.PROTOCOL, PullLanFollows.class);
		
		return supportedActions;
	}

	private static Map<Protocol, Class<?>> createSupportedEvents() {
		Map<Protocol, Class<?>> supportedEvents = new HashMap<>();
		supportedEvents.put(LanDeliveryIsDisabled.PROTOCOL, LanDeliveryIsDisabled.class);
		
		return supportedEvents;
	}
}
