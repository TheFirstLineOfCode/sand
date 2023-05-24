package com.thefirstlineofcode.sand.server.friends;

public class ReduplicateFollowException extends Exception {
	private static final long serialVersionUID = 6828387956934455010L;

	public ReduplicateFollowException() {
		super();
	}

	public ReduplicateFollowException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ReduplicateFollowException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReduplicateFollowException(String message) {
		super(message);
	}

	public ReduplicateFollowException(Throwable cause) {
		super(cause);
	}
	
}
