package com.thefirstlineofcode.sand.client.ibtr;

public class RegistrationException extends Exception {
	
	private static final long serialVersionUID = 3398560808264161877L;
	
	private IbtrError error;
	private Throwable cause;
	
	public RegistrationException(IbtrError error) {
		this(error, null);
	}
	
	public RegistrationException(IbtrError error, Throwable cause) {
		this.error = error;
		this.cause = cause;
	}

	public IbtrError getError() {
		return error;
	}

	public Throwable getCause() {
		return cause;
	}
	
	@Override
	public String getMessage() {
		return error.toString();
	}
	
}
