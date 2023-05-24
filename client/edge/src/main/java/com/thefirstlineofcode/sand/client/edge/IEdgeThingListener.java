package com.thefirstlineofcode.sand.client.edge;

import com.thefirstlineofcode.chalk.core.AuthFailureException;
import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.network.ConnectionException;
import com.thefirstlineofcode.sand.client.ibtr.RegistrationException;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

public interface IEdgeThingListener {
	void registered(ThingIdentity identity);
	void registerExceptionOccurred(RegistrationException e);
	void failedToAuth(AuthFailureException e);
	void FailedToConnect(ConnectionException e);
	void connectionExceptionOccurred(ConnectionException e);
	void connected(IChatClient chatClient);
	void disconnected();
}
