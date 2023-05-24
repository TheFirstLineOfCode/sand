package com.thefirstlineofcode.sand.client.webcam;

public interface IWatcher {
	public enum Status {
		CLOSED,
		OPENED,
		OFFERED,
		ANSWERED,
		WATCHING
	}
	
	void watch();
	void close();
	void opened();
	boolean isOpened();
	void closed();
	boolean isClosed();
	Status getStatus();
}
