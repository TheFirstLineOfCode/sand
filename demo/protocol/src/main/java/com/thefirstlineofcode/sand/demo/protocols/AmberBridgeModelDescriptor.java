package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.sand.protocols.thing.SimpleThingModelDescriptor;

public class AmberBridgeModelDescriptor extends SimpleThingModelDescriptor {
	public static final String MODEL_NAME = "Amber-Bridge";
	public static final String DESCRIPTION = "Amber bridge companion app";

	public AmberBridgeModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, null);
	}
}
