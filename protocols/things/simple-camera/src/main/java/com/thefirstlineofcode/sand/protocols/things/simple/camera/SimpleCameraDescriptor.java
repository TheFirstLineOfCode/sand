package com.thefirstlineofcode.sand.protocols.things.simple.camera;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class SimpleCameraDescriptor extends SimpleThingTypeDescriptor {
	public static final String TYPE_NAME = "simple-camera";
	
	public SimpleCameraDescriptor() {
		super(TYPE_NAME, false, null, null, createSupportedActions());
	}

	private static Map<Protocol, Class<?>> createSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		supportedActions.put(TakePhoto.PROTOCOL, TakePhoto.class);
		supportedActions.put(TakeVideo.PROTOCOL, TakeVideo.class);
		
		return supportedActions;
	}
}
