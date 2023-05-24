package com.thefirstlineofcode.sand.emulators.lora.things;

import com.thefirstlineofcode.sand.emulators.commons.AbstractThingEmulatorFactory;
import com.thefirstlineofcode.sand.emulators.commons.ICommunicationNetworkThingEmulatorFactory;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public abstract class AbstractLoraThingEmulatorFactory<T extends AbstractLoraThingEmulator> extends AbstractThingEmulatorFactory<T>
		implements ICommunicationNetworkThingEmulatorFactory<LoraAddress, LoraAddress, byte[], T> {	
	public AbstractLoraThingEmulatorFactory(String thingModel, String description) {
		super(thingModel, description);
	}

}
