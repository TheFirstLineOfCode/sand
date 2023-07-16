package com.thefirstlineofcode.sand.protocols.sensor;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ILanTraceable;

@ProtocolObject(namespace="urn:leps:tuxp:sensor", localName="lan-report")
public class LanReport implements ILanTraceable {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:sensor", "lan-report");
	
	@NotNull
	private byte[] traceId;
	private boolean ackRequired;
	private Object Data;
	
	public LanReport() {
		ackRequired = false;
	}
	
	public LanReport(byte[] traceId, Object data) {
		this(traceId, data, false);
	}
	
	public LanReport(byte[] traceId, Object data, boolean ackRequired) {
		if (traceId == null)
			throw new IllegalArgumentException("Null trace ID.");
		
		if (data == null)
			throw new IllegalArgumentException("Null data.");
		
		this.traceId = traceId;
		this.Data = data;
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

	public void setData(Object event) {
		this.Data = event;
	}
	
	public Object getData() {
		return Data;
	}
}
