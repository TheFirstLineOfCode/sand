package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.SimpleThingTypeDescriptor;

public class PineTimeDescriptor extends SimpleThingTypeDescriptor implements IThingTypeDescriptor {

	public PineTimeDescriptor() {
		super("pine-time", false, null, createSupportedData(), createSupportedAction());
		// TODO Auto-generated constructor stub
	}

	private static Map<Protocol, Class<?>> createSupportedAction() {
		// TODO Auto-generated method stub
		return null;
	}

	private static Map<Protocol, Class<?>> createSupportedData() {
		// TODO Auto-generated method stub
		return null;
	}

}
