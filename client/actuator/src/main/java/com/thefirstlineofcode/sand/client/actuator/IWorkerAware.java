package com.thefirstlineofcode.sand.client.actuator;

public interface IWorkerAware<T> {
	void setWorker(T worker);
}
