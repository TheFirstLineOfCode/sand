package com.thefirstlineofcode.sand.emulators.commons;

import com.thefirstlineofcode.sand.protocols.thing.IAddress;

public interface ICommunicationNetworkThingEmulatorFactory<OA, PA extends IAddress, D, T extends IThingEmulator> extends IThingEmulatorFactory<T> {
	T create();
}
