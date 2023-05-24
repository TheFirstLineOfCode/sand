package com.thefirstlineofcode.sand.emulators.commons;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.thing.IAddress;

public interface ICommunicationNetworkThingEmulator<OA extends IAddress, PA extends IAddress, D> extends IThingEmulator {
	void startToReceiveData();
	void stopDataReceving();
	ICommunicator<OA, PA, D> getCommunicator();
}
