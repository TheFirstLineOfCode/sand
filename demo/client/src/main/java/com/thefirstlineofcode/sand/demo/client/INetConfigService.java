package com.thefirstlineofcode.sand.demo.client;

import java.util.Date;

public interface INetConfigService {
	public interface NetConfigEventsListener {
		void tryToRegisterWithoutAuthoration(String thingId);
		void thingRegistered(String thingId, String thingName, String authorizer, Date registrationTime);
		void requestToConfirm(String concentratorThingName, String nodeThingId, Date requestedTime);
		void nodeAdded(String concentratorThingName, String nodeThingId, int lanId, Date addedTime);
	}
	
	void startToListenNetConfigEvents(NetConfigEventsListener listener);
	void stopToListenNetConfigEvents();
}
