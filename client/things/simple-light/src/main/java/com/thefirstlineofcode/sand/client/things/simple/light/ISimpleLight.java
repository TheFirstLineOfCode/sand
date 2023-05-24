package com.thefirstlineofcode.sand.client.things.simple.light;

import com.thefirstlineofcode.sand.client.thing.IThing;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;

public interface ISimpleLight extends IThing {	
	public enum LightState {
		ON,
		OFF
	}
	
	public static final int ERROR_CODE_NOT_REMOTE_CONTROL_STATE = -1;
	public static final int ERROR_CODE_INVALID_REPEAT_ATTRIBUTE_VALUE = -2;
	
	SwitchState getSwitchState();
	LightState getLightState();
	void turnOn() throws ExecutionException;
	void turnOff() throws ExecutionException;
	void flash(int repeat) throws ExecutionException;
	void fireSwitchChangedEvent(SwitchState previous, SwitchState current);
}
