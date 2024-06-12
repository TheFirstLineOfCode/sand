package com.thefirstlineofcode.sand.client.thing.commuication;

import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public interface IAddressConfigurator<C extends ICommunicator<?, PA, D>, PA extends ILanAddress, D> {
	void setCommunicator(C communicator);
	void introduce();
	void negotiate(PA peerAddress, D data) throws CommunicationException;
}
