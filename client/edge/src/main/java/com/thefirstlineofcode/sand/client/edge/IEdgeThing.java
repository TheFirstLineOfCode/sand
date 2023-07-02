package com.thefirstlineofcode.sand.client.edge;

import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.network.IConnectionListener;
import com.thefirstlineofcode.sand.client.thing.IThing;

public interface IEdgeThing extends IThing {
	StandardStreamConfig getStreamConfig();
	void setStreamConfig(StandardStreamConfig streamConfig);
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
