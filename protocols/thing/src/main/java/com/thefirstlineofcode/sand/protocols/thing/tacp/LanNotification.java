package com.thefirstlineofcode.sand.protocols.thing.tacp;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:thing", localName="lan-notification")
public class LanNotification implements ILanTraceable {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:thing", "lan-notification");
	
	@NotNull
	private byte[] traceId;
	private boolean ackRequired;
	private Object event;
	
	public LanNotification() {
		ackRequired = false;
	}
	
	public LanNotification(byte[] traceId, Object event) {
		this(traceId, event, false);
	}
	
	public LanNotification(byte[] traceId, Object event, boolean ackRequired) {
		if (traceId == null)
			throw new IllegalArgumentException("Null trace ID.");
		
		if (event == null)
			throw new IllegalArgumentException("Null event.");
		
		this.traceId = traceId;
		this.event = event;
		this.ackRequired = ackRequired;
	}
	
	public void setTraceId(byte[] traceId) {
		this.traceId = traceId;
	}
	
	@Override
	public byte[] getTraceId() {
		return traceId;
	}
	
	public boolean isAckRequired() {
		return ackRequired;
	}

	public void setAckRequired(boolean ackRequired) {
		this.ackRequired = ackRequired;
	}

	public void setEvent(Object event) {
		this.event = event;
	}
	
	public Object getEvent() {
		return event;
	}
}
