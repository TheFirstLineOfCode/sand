package com.thefirstlineofcode.sand.emulators.models;

import com.thefirstlineofcode.sand.protocols.thing.SingleTypeThingModelDescriptor;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SimpleLightDescriptor;

public class Sle02ModelDescriptor extends SingleTypeThingModelDescriptor {
	public static final String MODEL_NAME = "SLE-02";
	public static final String DESCRIPTION = "Simple Light WiFi Emulator";

	public Sle02ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new SimpleLightDescriptor());
	}
}
