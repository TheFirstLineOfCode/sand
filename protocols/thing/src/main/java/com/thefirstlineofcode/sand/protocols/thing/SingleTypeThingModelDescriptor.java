package com.thefirstlineofcode.sand.protocols.thing;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public class SingleTypeThingModelDescriptor implements IThingModelDescriptor {
	private String modelName;
	private String description;
	private IThingTypeDescriptor typeDescriptor;
	
	public SingleTypeThingModelDescriptor(String modelName, String description, IThingTypeDescriptor typeDescriptor) {
		this.modelName = modelName;
		this.description = description;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public boolean isActuator() {
		return typeDescriptor.isActuator();
	}

	@Override
	public boolean isConcentrator() {
		return typeDescriptor.isConcentrator();
	}

	@Override
	public boolean isSensor() {
		return typeDescriptor.isSensor();
	}

	@Override
	public Map<Protocol, Class<?>> getSupportedEvents() {
		return typeDescriptor.getSupportedEvents();
	}

	@Override
	public Map<Protocol, Class<?>> getSupportedData() {
		return typeDescriptor.getSupportedData();
	}

	@Override
	public Map<Protocol, Class<?>> getSupportedActions() {
		return typeDescriptor.getSupportedActions();
	}

	@Override
	public String getModelName() {
		return modelName;
	}
	
	public IThingTypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Map<Protocol, Class<?>> getFollowedEvents() {
		return typeDescriptor.getFollowedEvents();
	}
}
