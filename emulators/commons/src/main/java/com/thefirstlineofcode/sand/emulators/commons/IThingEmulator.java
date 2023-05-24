package com.thefirstlineofcode.sand.emulators.commons;

import com.thefirstlineofcode.sand.client.thing.IThing;
import com.thefirstlineofcode.sand.emulators.commons.ui.AbstractThingEmulatorPanel;

public interface IThingEmulator extends IThing {
	void powerOn();
	void powerOff();
	void reset();
	AbstractThingEmulatorPanel<?> getPanel();
	String getThingStatus();
	long getElapsedTime();
}
