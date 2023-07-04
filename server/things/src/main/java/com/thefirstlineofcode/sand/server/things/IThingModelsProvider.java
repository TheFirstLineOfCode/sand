package com.thefirstlineofcode.sand.server.things;

import org.pf4j.ExtensionPoint;

import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;

public interface IThingModelsProvider extends ExtensionPoint {
	IThingModelDescriptor[] provide();
}
