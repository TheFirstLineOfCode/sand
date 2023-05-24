package com.thefirstlineofcode.sand.protocols.thing;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public class SimpleThingTypeDescriptor extends AbstractThingDescriptor implements IThingTypeDescriptor {
	protected String typeName;
	
	public SimpleThingTypeDescriptor(String typeName, Map<Protocol, Class<?>> supportedEvents) {
		this(typeName, false, supportedEvents);
	}
	
	public SimpleThingTypeDescriptor(String typeName, boolean concentrator, Map<Protocol, Class<?>> supportedEvents) {
		this(typeName, concentrator, supportedEvents, null, null);
	}
	
	public SimpleThingTypeDescriptor(String typeName, boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData) {
		this(typeName, concentrator, supportedEvents, supportedData, null);
	}
	
	public SimpleThingTypeDescriptor(String typeName, boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData, Map<Protocol, Class<?>> suppportedActions) {
		super(concentrator, supportedEvents, supportedData, suppportedActions);
		this.typeName = typeName;
	}
	
	@Override
	public String getTypeName() {
		return typeName;
	}

}
