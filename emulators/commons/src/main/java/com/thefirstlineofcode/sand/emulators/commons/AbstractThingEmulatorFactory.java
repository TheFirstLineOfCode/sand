package com.thefirstlineofcode.sand.emulators.commons;

public abstract class AbstractThingEmulatorFactory<T extends IThingEmulator> implements IThingEmulatorFactory<T> {
	protected String thingModel;
	protected String description;
	
	public AbstractThingEmulatorFactory(String thingModel, String description) {
		this.thingModel = thingModel;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getThingModel() {
		return thingModel;
	}

}
