package com.thefirstlineofcode.sand.demo.app.android;

public class WatchException extends Exception {
	public WatchException() {
	}
	
	public WatchException(String message) {
		super(message);
	}
	
	public WatchException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public WatchException(Throwable cause) {
		super(cause);
	}
	
	public WatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
