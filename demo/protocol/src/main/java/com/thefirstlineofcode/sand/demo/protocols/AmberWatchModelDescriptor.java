package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlinelinecode.sand.protocols.concentrator.ConcentratorDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.MultiTypeThingModeDescriptor;

public class AmberWatchModelDescriptor extends MultiTypeThingModeDescriptor  {
	public static final String MODEL_NAME = "Amber-Watch";
	public static final String DESCRIPTION = "Amber smart watch";

	public AmberWatchModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new IThingTypeDescriptor[] {
			new ConcentratorDescriptor(),
			new PineTimeDescriptor()
		});
	}

}
