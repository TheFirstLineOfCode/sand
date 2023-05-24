package com.thefirstlineofcode.sand.client.thing.commuication;

import com.thefirstlineofcode.sand.protocols.thing.IAddress;

public interface ICommunicationListener<OA, PA extends IAddress, D> {
	void sent(PA to, D data);
	void received(PA from, D data);
	void occurred(CommunicationException e);
	void addressChanged(OA newAddress, OA oldAddress);
}
