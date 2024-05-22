package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.sand.protocols.thing.SingleTypeThingModelDescriptor;

public class AmberWatchModelDescriptor extends SingleTypeThingModelDescriptor  {
	public static final String MODEL_NAME = "Amber-Watch";
	public static final String DESCRIPTION = "Amber smart watch";

	public AmberWatchModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new PineTimeDescriptor());
	}

}
