package com.thefirstlineofcode.sand.protocols.actuator;

public class ExecutionException extends Exception {
	private static final long serialVersionUID = 4957236030809756512L;
	
	private int errorNumber;
	
	public ExecutionException(int errorNumber) {
		this.errorNumber = errorNumber;
	}

	public int getErrorNumber() {
		return errorNumber;
	}
	
	@Override
	public String getMessage() {
		return String.format("Execution exception. Error number: %s.", errorNumber);
	}

}
