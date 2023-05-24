package com.thefirstlineofcode.sand.server.sensor;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;

public interface IDataListener<T> {
	void dataReceived(IProcessingContext context, JabberId reporter, T data);
}
