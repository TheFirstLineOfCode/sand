package com.thefirstlineofcode.sand.client.ibtr;

import com.thefirstlineofcode.chalk.core.stream.INegotiationListener;
import com.thefirstlineofcode.chalk.network.IConnectionListener;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;

public interface IRegistration {
	RegisteredEdgeThing register(String thingId, String registrationCode) throws RegistrationException;
	void remove();
	void addConnectionListener(IConnectionListener listener);
	void removeConnectionListener(IConnectionListener listener);
	void addNegotiationListener(INegotiationListener listener);
	void removeNegotiationListener(INegotiationListener listener);
}
