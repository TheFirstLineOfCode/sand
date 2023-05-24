package com.thefirstlineofcode.sand.protocols.thing.tacp;

public interface ITraceId {
	public enum Type {
		REQUEST,
		RESPONSE,
		ERROR
	}
	
	byte[] getBytes();
	Type getType();
	boolean isAnswer(ITraceId answer);
	boolean isAnswerId(byte[] answerId);
	ITraceId createResponseId();
	ITraceId createErrorId();
}
