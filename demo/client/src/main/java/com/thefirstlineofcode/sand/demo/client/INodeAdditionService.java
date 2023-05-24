package com.thefirstlineofcode.sand.demo.client;

import java.util.Date;

import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;

public interface INodeAdditionService {
	public interface Listener {
		void requestToConfirm(String concentratorThingName, String nodeThingId, CommunicationNet net);
		void nodeAdded(String concentratorThingName, String nodeThingId, Date addedTime);
	}
	
	void startToListen(Listener listener);
	void stopToListen();
}
