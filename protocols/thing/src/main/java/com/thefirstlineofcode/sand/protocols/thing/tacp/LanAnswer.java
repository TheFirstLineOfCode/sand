package com.thefirstlineofcode.sand.protocols.thing.tacp;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:thing", localName="lan-answer")
public class LanAnswer {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:thing", "lan-answer");
	
	private byte[] traceId;
	private Integer errorNumber;
	
	public LanAnswer() {}
	
	public LanAnswer(byte[] traceId) {
		this(traceId, null);
	}
	
	public LanAnswer(byte[] traceId, Integer errorNumber) {
		if (traceId == null)
			throw new IllegalArgumentException("Null trace ID>");
		
		if (ThingsTinyId.getType(traceId) != ITraceId.Type.ERROR &&
				errorNumber != null)
			throw new IllegalArgumentException("Error number is only allowed in error type tiny ID.");
		
		this.traceId = traceId;
		this.errorNumber = errorNumber;
	}
	
	public byte[] getTraceId() {
		return traceId;
	}
	
	public void setTraceId(byte[] traceId) {
		this.traceId = traceId;
	}

	public Integer getErrorNumber() {
		return errorNumber;
	}

	public void setErrorNumber(Integer errorNumber) {
		this.errorNumber = errorNumber;
	}
}
