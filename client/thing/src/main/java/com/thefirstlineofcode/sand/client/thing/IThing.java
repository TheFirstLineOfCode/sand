package com.thefirstlineofcode.sand.client.thing;

import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;

public interface IThing {
	String getThingId();
	String getThingModel();
	String getSoftwareVersion();
	int getBatteryPower();
	boolean isPowered();
	
	void addBatteryPowerListener(IBatteryPowerListener listener);
	boolean removeBatteryPowerListener(IBatteryPowerListener listener);
	
	void start();
	boolean isStarted();
	
	void stop() throws ExecutionException;
	boolean isStopped();
	
	void shutdownSystem(boolean restart) throws ExecutionException;
}
