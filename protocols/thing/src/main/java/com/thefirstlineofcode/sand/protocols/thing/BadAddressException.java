package com.thefirstlineofcode.sand.protocols.thing;

public class BadAddressException extends Exception {
	private static final long serialVersionUID = 1378030572959348900L;

	public BadAddressException() {
		super();
	}

	public BadAddressException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BadAddressException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadAddressException(String message) {
		super(message);
	}

	public BadAddressException(Throwable cause) {
		super(cause);
	}
		
}
