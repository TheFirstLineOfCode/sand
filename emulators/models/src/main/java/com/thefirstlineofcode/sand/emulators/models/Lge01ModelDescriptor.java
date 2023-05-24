package com.thefirstlineofcode.sand.emulators.models;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.edge.EdgeThingDescriptor;
import com.thefirstlineofcode.sand.protocols.lora.gateway.LoraGatewayDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.MultiTypeThingModeDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;

public class Lge01ModelDescriptor extends MultiTypeThingModeDescriptor {
	public static final String MODEL_NAME = "LGE-01";
	public static final String DESCRIPTION = "Lora gateway emulator";
	
	private Map<Protocol, Class<?>> followedEvents;

	public Lge01ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION,
			new IThingTypeDescriptor[] {
				new EdgeThingDescriptor(),
				new LoraGatewayDescriptor()
			}
		);
		
		followedEvents = new HashMap<>();
		followedEvents.put(SwitchStateChanged.PROTOCOL, SwitchStateChanged.class);
	}
	
	@Override
	public Map<Protocol, Class<?>> getFollowedEvents() {
		return followedEvents;
	}
}
