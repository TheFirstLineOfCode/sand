package com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class SimpleTemperatureReporterDescriptor extends SimpleThingTypeDescriptor {
	public static final String TYPE_NAME = "simple-temperature-reporter";
	
	public SimpleTemperatureReporterDescriptor() {
		super(TYPE_NAME, false, null, createSupportedData(), null);
	}

	private static Map<Protocol, Class<?>> createSupportedData() {
		Map<Protocol, Class<?>> supportedData = new HashMap<>();
		supportedData.put(CelsiusDegree.PROTOCOL, CelsiusDegree.class);
		
		return supportedData;
	}
}
