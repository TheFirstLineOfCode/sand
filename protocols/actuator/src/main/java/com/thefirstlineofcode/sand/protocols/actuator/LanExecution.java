package com.thefirstlineofcode.sand.protocols.actuator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ILanTraceable;

@ProtocolObject(namespace="urn:leps:tuxp:actuator", localName="lan-execution")
public class LanExecution implements ILanTraceable {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:actuator", "lan-execution");
	
	@NotNull
	private byte[] traceId;
	private Object action;
	
	public LanExecution() {}
	
	public LanExecution(byte[] traceId) {
		this(traceId, null);
	}
	
	public LanExecution(byte[] traceId, Object action) {
		this.traceId = traceId;
		this.action = action;
	}
	
	public void setTraceId(byte[] traceId) {
		this.traceId = traceId;
	}
	
	@Override
	public byte[] getTraceId() {
		return traceId;
	}
	
	public void setAction(Object action) {
		this.action = action;
	}
	
	public Object getAction() {
		return action;
	}
}
