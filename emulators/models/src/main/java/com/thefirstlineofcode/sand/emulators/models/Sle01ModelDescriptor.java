package com.thefirstlineofcode.sand.emulators.models;

import java.util.LinkedHashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SingleTypeThingModelDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ResetThing;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SimpleLightDescriptor;

public class Sle01ModelDescriptor extends SingleTypeThingModelDescriptor {
	public static final String MODEL_NAME = "SLE-01";
	public static final String DESCRIPTION = "Simple Light Lora Emulator";

	public Sle01ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new SimpleLightDescriptor());
	}
	
	@Override
	public Map<Protocol, Class<?>> getSupportedActions() {
		Map<Protocol, Class<?>> allSupportedActions = new LinkedHashMap<>();
		allSupportedActions.putAll(super.getSupportedActions());
		allSupportedActions.put(ResetThing.PROTOCOL, ResetThing.class);
		
		return allSupportedActions;
	}
}
