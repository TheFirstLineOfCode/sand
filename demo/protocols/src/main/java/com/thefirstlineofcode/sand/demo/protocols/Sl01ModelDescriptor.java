package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.sand.protocols.thing.SingleTypeThingModelDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SimpleLightDescriptor;

public class Sl01ModelDescriptor extends SingleTypeThingModelDescriptor {
	public static final String MODEL_NAME = "SL-01";
	public static final String DESCRIPTION = "Simple Light on Raspberry Pi";
	
	public Sl01ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new SimpleLightDescriptor());
	}
}
