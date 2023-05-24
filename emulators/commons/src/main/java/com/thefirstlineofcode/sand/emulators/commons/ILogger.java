package com.thefirstlineofcode.sand.emulators.commons;

public interface ILogger {
	String getName();
	void log(String message);
	void log(Exception e);
}
