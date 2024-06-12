package com.thefirstlineofcode.sand.emulators.commons;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public interface ICommunicationNetworkThingEmulator<OA extends ILanAddress, PA extends ILanAddress, D> extends IThingEmulator {
	void startToReceiveData();
	void stopDataReceving();
	ICommunicator<OA, PA, D> getCommunicator();
}
