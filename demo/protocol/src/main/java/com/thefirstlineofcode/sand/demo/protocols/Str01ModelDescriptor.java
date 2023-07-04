package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.MultiTypeThingModeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraThingDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter.SimpleTemperatureReporterDescriptor;

public class Str01ModelDescriptor extends MultiTypeThingModeDescriptor {
	public static final String MODEL_NAME = "STR-01";
	public static final String DESCRIPTION = "Simple temperature reporter on Arduino";
	
	public Str01ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new IThingTypeDescriptor[] {
				new LoraThingDescriptor(),
				new SimpleTemperatureReporterDescriptor()
		});
	}
}
