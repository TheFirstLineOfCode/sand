package com.thefirstlineofcode.sand.client.edge;

import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.core.stream.StreamConfig;
import com.thefirstlineofcode.chalk.network.IConnectionListener;
import com.thefirstlineofcode.sand.client.thing.IThing;

public interface IEdgeThing extends IThing {
	StreamConfig getStreamConfig();
	boolean isRegistered();
	void register();
	boolean isConnected();
	void connect();
	void addEdgeThingListener(IEdgeThingListener edgeThingListener);
	boolean removeEdgeThingListener(IEdgeThingListener edgeThingListener);
	void addConnectionListener(IConnectionListener connectionListener);
	boolean removeConnectionListener(IConnectionListener connectionListener);
	IChatClient getChatClient();
}
