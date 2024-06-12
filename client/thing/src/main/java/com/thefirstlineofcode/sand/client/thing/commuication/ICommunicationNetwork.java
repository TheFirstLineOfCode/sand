package com.thefirstlineofcode.sand.client.thing.commuication;

import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public interface ICommunicationNetwork<A extends ILanAddress, D, P extends ParamsMap> {
	ICommunicationChip<A, D> createChip(A address, P params);
	void sendData(ICommunicationChip<A, D> from, A to, byte[] data);
	Data<A, D> receiveData(ICommunicationChip<A, D> target);
	void changeAddress(ICommunicationChip<A, D> chip, A newAddress);
	void addListener(ICommunicationNetworkListener<A, D> listener);
	boolean removeListener(ICommunicationNetworkListener<A, D> listener);
}
