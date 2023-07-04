package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.sand.protocols.lora.gateway.LoraGatewayDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.SingleTypeThingModelDescriptor;

public class Lg01ModelDescriptor extends SingleTypeThingModelDescriptor {
	public static final String MODEL_NAME = "LG-01";
	public static final String DESCRIPTION = "Lora Gateway on Raspberry PI";

	public Lg01ModelDescriptor() {
		super(MODEL_NAME, DESCRIPTION, new LoraGatewayDescriptor());
	}
}
