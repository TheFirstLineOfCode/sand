package com.thefirstlineofcode.sand.client.thing.commuication;

import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public interface ICommunicator<OA extends ILanAddress, PA extends ILanAddress, D> {
	void initialize();
	boolean isInitialized();
	void configure();
	void changeAddress(OA address, boolean savePersistently) throws CommunicationException;
	OA getAddress();
	void send(PA to, D data) throws CommunicationException;
	void received(PA from, D data);
	void addCommunicationListener(ICommunicationListener<OA, PA, D> listener);
	void removeCommunicationListener(ICommunicationListener<OA, PA, D> listener);
	void startToListen();
	void stopToListen();
	boolean isListening();
}
