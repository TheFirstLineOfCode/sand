package com.thefirstlineofcode.sand.protocols.thing;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public class SimpleThingModelDescriptor extends AbstractThingDescriptor implements IThingModelDescriptor {
	private String modelName;
	private String description;
	
	public SimpleThingModelDescriptor(String modelName, String description, Map<Protocol, Class<?>> supportedEvents) {
		this(modelName, description, false, supportedEvents);
	}
	
	public SimpleThingModelDescriptor(String modelName, String description, boolean concentrator, Map<Protocol, Class<?>> supportedEvents) {
		this(modelName, description, concentrator, supportedEvents, null, null);
	}
	
	public SimpleThingModelDescriptor(String modelName, String description, boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData) {
		this(modelName, description, concentrator, supportedEvents, supportedData, null);
	}
	
	public SimpleThingModelDescriptor(String modelName, String description, boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData, Map<Protocol, Class<?>> suppportedActions) {
		super(concentrator, supportedEvents, supportedData, suppportedActions);
		this.modelName = modelName;
		this.description = description;
	}
	
	public SimpleThingModelDescriptor(String modelName, String description, boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData, Map<Protocol, Class<?>> suppportedActions,
				Map<Protocol, Class<?>> suppportedActionResults) {
		super(concentrator, supportedEvents, supportedData, suppportedActions, suppportedActionResults);
		this.modelName = modelName;
		this.description = description;
	}
	
	@Override
	public String getModelName() {
		return modelName;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
