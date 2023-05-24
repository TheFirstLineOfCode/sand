package com.thefirstlineofcode.sand.emulators.commons;

import com.thefirstlineofcode.sand.client.things.simple.light.ISimpleLight;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;

public interface ISimpleLightEmulator extends ISimpleLight, IThingEmulator {
	void changeSwitchState(SwitchState switchState);
}
