package com.thefirstlineofcode.sand.client.sensor;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;

public interface IDataProcessor<T> {
	void processData(JabberId reporter, T data);
}
