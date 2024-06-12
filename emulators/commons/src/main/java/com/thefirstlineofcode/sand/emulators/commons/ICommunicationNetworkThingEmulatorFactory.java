package com.thefirstlineofcode.sand.emulators.commons;

import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public interface ICommunicationNetworkThingEmulatorFactory<OA, PA extends ILanAddress, D, T extends IThingEmulator> extends IThingEmulatorFactory<T> {
	T create();
}
