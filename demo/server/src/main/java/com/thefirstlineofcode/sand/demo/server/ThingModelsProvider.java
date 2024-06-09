package com.thefirstlineofcode.sand.demo.server;

import org.pf4j.Extension;

import com.thefirstlineofcode.amber.protocol.AmberBridgeModelDescriptor;
import com.thefirstlineofcode.amber.protocol.AmberWatchModelDescriptor;
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
	public IThingModelDescriptor[] provide() {
		return new IThingModelDescriptor[] {
			new Lge01ModelDescriptor(),
			new Sle01ModelDescriptor(),
			new Sle02ModelDescriptor(),
			new Lgsc01ModelDescriptor(),
			new Sl01ModelDescriptor(),
			new Sl02ModelDescriptor(),
			new Lg01ModelDescriptor(),
			new Str01ModelDescriptor(),
			new AmberBridgeModelDescriptor(),
			new AmberWatchModelDescriptor()
		};		
	}
}
