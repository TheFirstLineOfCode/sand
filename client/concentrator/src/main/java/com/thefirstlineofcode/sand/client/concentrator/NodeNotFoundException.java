package com.thefirstlineofcode.sand.client.concentrator;

public class NodeNotFoundException extends Exception {
	private static final long serialVersionUID = 325123678253896806L;

	public NodeNotFoundException() {
		super();
	}

	public NodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NodeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeNotFoundException(String message) {
		super(message);
	}

	public NodeNotFoundException(Throwable cause) {
		super(cause);
	}	
}
