package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.edge.EdgeThingDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.SingleTypeThingModelDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.light.Flash;
import com.thefirstlineofcode.sand.protocols.things.simple.light.TurnOff;
import com.thefirstlineofcode.sand.protocols.things.simple.light.TurnOn;

public class Sl01ModelDescriptor extends SingleTypeThingModelDescriptor {
	public static final String MODEL_NAME = "SL-01";
	public static final String DESCRIPTION = "Simple Light on Raspberry Pi";
	
	public Sl01ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new EdgeThingDescriptor());
	}
	
	@Override
	public Map<Protocol, Class<?>> getSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = super.getSupportedActions();
		
		supportedActions.put(Flash.PROTOCOL, Flash.class);
		supportedActions.put(TurnOn.PROTOCOL, TurnOn.class);
		supportedActions.put(TurnOff.PROTOCOL, TurnOff.class);
		
		return supportedActions;
	}
}
