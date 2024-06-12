package com.thefirstlineofcode.sand.client.thing.commuication;

import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public interface ICommunicationChip<A extends ILanAddress, D> {
	void changeAddress(A address) throws CommunicationException;
	A getAddress();
	void send(A to, D data) throws CommunicationException;
	Data<A, D> receive();
	void addListener(ICommunicationListener<A, A, D> listener);
	boolean removeListener(ICommunicationListener<A, A, D> listener);
}
