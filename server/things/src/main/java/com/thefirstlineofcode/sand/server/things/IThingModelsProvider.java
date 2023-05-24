package com.thefirstlineofcode.sand.server.things;

import java.util.List;

import org.pf4j.ExtensionPoint;

import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;

public interface IThingModelsProvider extends ExtensionPoint {
	List<IThingModelDescriptor> provide();
}
