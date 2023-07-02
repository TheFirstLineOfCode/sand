package com.thefirstlineofcode.sand.client.ibtr;

import com.thefirstlineofcode.chalk.core.stream.INegotiationListener;
import com.thefirstlineofcode.chalk.network.IConnectionListener;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;

public interface IRegistration {
	RegisteredThing register(String thingId, String registrationKey) throws RegistrationException;
	void remove();
	void addConnectionListener(IConnectionListener listener);
	void removeConnectionListener(IConnectionListener listener);
	void addNegotiationListener(INegotiationListener listener);
	void removeNegotiationListener(INegotiationListener listener);
}
