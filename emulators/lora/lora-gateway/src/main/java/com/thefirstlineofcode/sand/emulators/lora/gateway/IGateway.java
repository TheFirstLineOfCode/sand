package com.thefirstlineofcode.sand.emulators.lora.gateway;

import com.thefirstlineofcode.sand.client.thing.IThing;

public interface IGateway extends IThing {
	boolean isRegistered();
	boolean isConnected();
	void register();
	void connect();
	void disconnect();
	void setToRouterMode();
	void setToDacMode();
	int getLanId();
}
