package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.edge.EdgeThingDescriptor;
import com.thefirstlineofcode.sand.protocols.lora.gateway.LoraGatewayDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.MultiTypeThingModeDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.SimpleCameraDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;

public class Lgsc01ModelDescriptor extends MultiTypeThingModeDescriptor {
	public static final String MODEL_NAME = "LGSC-01";
	public static final String DESCRIPTION = "Lora gateway&Simple camera on Raspberry PI";
	
	public Lgsc01ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new IThingTypeDescriptor[] {
			new EdgeThingDescriptor(),
			new SimpleCameraDescriptor(),
			new LoraGatewayDescriptor()
		});
	}
	
	@Override
	public Map<Protocol, Class<?>> getFollowedEvents() {
		Map<Protocol, Class<?>> followedEvents = super.getFollowedEvents();
		followedEvents.put(SwitchStateChanged.PROTOCOL, SwitchStateChanged.class);
		
		return followedEvents;
	}
	
	@Override
	public Map<Protocol, Class<?>> getSupportedEvents() {
		Map<Protocol, Class<?>> supportedEvents = super.getSupportedEvents();
		supportedEvents.put(VideoRecorded.PROTOCOL, VideoRecorded.class);
		
		return supportedEvents;
	}
}
