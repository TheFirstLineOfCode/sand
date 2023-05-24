package com.thefirstlineofcode.sand.demo.server;

import java.util.ArrayList;
import java.util.List;

import org.pf4j.Extension;

import com.thefirstlineofcode.sand.demo.protocols.Lg01ModelDescriptor;
import com.thefirstlineofcode.sand.demo.protocols.Lgsc01ModelDescriptor;
import com.thefirstlineofcode.sand.demo.protocols.Sl01ModelDescriptor;
import com.thefirstlineofcode.sand.demo.protocols.Sl02ModelDescriptor;
import com.thefirstlineofcode.sand.demo.protocols.Str01ModelDescriptor;
import com.thefirstlineofcode.sand.emulators.models.Lge01ModelDescriptor;
import com.thefirstlineofcode.sand.emulators.models.Sle01ModelDescriptor;
import com.thefirstlineofcode.sand.emulators.models.Sle02ModelDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;
import com.thefirstlineofcode.sand.server.things.IThingModelsProvider;

@Extension
public class ThingModelsProvider implements IThingModelsProvider {

	@Override
	public List<IThingModelDescriptor> provide() {
		List<IThingModelDescriptor> models = new ArrayList<>();
		
		models.add(new Lge01ModelDescriptor());
		models.add(new Sle01ModelDescriptor());
		models.add(new Sle02ModelDescriptor());		
		models.add(new Lgsc01ModelDescriptor());
		models.add(new Sl01ModelDescriptor());
		models.add(new Sl02ModelDescriptor());
		models.add(new Lg01ModelDescriptor());
		models.add(new Str01ModelDescriptor());
		
		return models;
	}
}
