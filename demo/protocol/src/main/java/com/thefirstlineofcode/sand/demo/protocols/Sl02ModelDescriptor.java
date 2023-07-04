package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.MultiTypeThingModeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraThingDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SimpleLightDescriptor;

public class Sl02ModelDescriptor extends MultiTypeThingModeDescriptor {
	public static final String MODEL_NAME = "SL-02";
	public static final String DESCRIPTION = "Simple LoRa light on Arduino";
	
	public Sl02ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new IThingTypeDescriptor[] {
				new LoraThingDescriptor(),
				new SimpleLightDescriptor()
		});
	}
}
