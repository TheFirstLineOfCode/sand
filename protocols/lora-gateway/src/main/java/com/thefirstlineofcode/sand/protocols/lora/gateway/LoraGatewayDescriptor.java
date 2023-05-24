package com.thefirstlineofcode.sand.protocols.lora.gateway;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlinelinecode.sand.protocols.concentrator.ConcentratorDescriptor;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.lora.dac.LoraDacServiceDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.MultiTypeThingTypeDescriptor;

public class LoraGatewayDescriptor extends MultiTypeThingTypeDescriptor {
	public static final String TYPE_NAME = "lora-gateway";

	public LoraGatewayDescriptor() {
		super(TYPE_NAME,
			new IThingTypeDescriptor[] {
				new LoraDacServiceDescriptor(),
				new ConcentratorDescriptor()
			}
		);
	}
	
	@Override
	public Map<Protocol, Class<?>> getSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = new HashMap<>();
		if (super.getSupportedActions() != null)
			supportedActions.putAll(super.getSupportedActions());
		
		supportedActions.put(ChangeWorkingMode.PROTOCOL, ChangeWorkingMode.class);
		
		return supportedActions;
	}
}
