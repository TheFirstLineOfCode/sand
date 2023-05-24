package com.thefirstlineofcode.sand.client.thing.commuication;

public interface ICommunicatorFactory {
	ICommunicator<?, ?, ?> createCommunicator(ParamsMap params);
}
