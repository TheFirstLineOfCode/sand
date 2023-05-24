package com.thefirstlineofcode.sand.protocols.thing;

import java.util.LinkedHashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public class MultiTypeThingModeDescriptor implements IThingModelDescriptor {
	private String modelName;
	private String description;
	private IThingTypeDescriptor[] typeDescriptors;
	
	public MultiTypeThingModeDescriptor(String modelName, String description, IThingTypeDescriptor[] typeDescriptors) {
		this.modelName = modelName;
		this.description = description;
		
		if (typeDescriptors == null)
			throw new IllegalArgumentException("Null type descripotors.");
		this.typeDescriptors = typeDescriptors;
	}
	

	@Override
	public boolean isActuator() {
		for (IThingTypeDescriptor typeDescriptor : typeDescriptors) {
			if (typeDescriptor.isActuator())
				return true;
		}
		
		return false;
	}

	@Override
	public boolean isConcentrator() {
		for (IThingTypeDescriptor typeDescriptor : typeDescriptors) {
			if (typeDescriptor.isConcentrator())
				return true;
		}
		
		return false;
	}

	@Override
	public boolean isSensor() {
		for (IThingTypeDescriptor typeDescriptor : typeDescriptors) {
			if (typeDescriptor.isSensor())
				return true;
		}
		
		return false;
	}

	@Override
	public Map<Protocol, Class<?>> getSupportedEvents() {
		Map<Protocol, Class<?>> allSupportedEvents = new LinkedHashMap<>();
		for (IThingTypeDescriptor typeDescriptor : typeDescriptors) {
			if (typeDescriptor.getSupportedEvents() != null)
				allSupportedEvents.putAll(typeDescriptor.getSupportedEvents());
		}
		
		return allSupportedEvents;
	}
	
	@Override
	public Map<Protocol, Class<?>> getFollowedEvents() {
		Map<Protocol, Class<?>> allFollowedEvents = new LinkedHashMap<>();
		for (IThingTypeDescriptor typeDescriptor : typeDescriptors) {
			if (typeDescriptor.getFollowedEvents() != null)
				allFollowedEvents.putAll(typeDescriptor.getFollowedEvents());
		}
		
		return allFollowedEvents;
	}

	@Override
	public Map<Protocol, Class<?>> getSupportedData() {
		Map<Protocol, Class<?>> allSupportedData = new LinkedHashMap<>();
		for (IThingTypeDescriptor typeDescriptor : typeDescriptors) {
			if (typeDescriptor.getSupportedData() != null)
				allSupportedData.putAll(typeDescriptor.getSupportedData());
		}
		
		return allSupportedData;
	}

	@Override
	public Map<Protocol, Class<?>> getSupportedActions() {
		Map<Protocol, Class<?>> allSupportedActions = new LinkedHashMap<>();
		for (IThingTypeDescriptor typeDescriptor : typeDescriptors) {
			if (typeDescriptor.getSupportedActions() != null)
				allSupportedActions.putAll(typeDescriptor.getSupportedActions());
		}
		
		return allSupportedActions;
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
