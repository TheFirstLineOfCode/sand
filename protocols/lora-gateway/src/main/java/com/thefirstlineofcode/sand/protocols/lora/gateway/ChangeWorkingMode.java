package com.thefirstlineofcode.sand.protocols.lora.gateway;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.Int2Enum;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:lora-gateway", localName="change-working-mode")
public class ChangeWorkingMode {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:lora-gateway", "change-working-mode");
	
	@Int2Enum(WorkingMode.class)
	private WorkingMode workingMode;
	
	public ChangeWorkingMode() {
		this(WorkingMode.ROUTER);
	}
	
	public ChangeWorkingMode(WorkingMode workingMode) {
		this.workingMode = workingMode;
	}
	
	public WorkingMode getWorkingMode() {
		return workingMode;
	}

	public void setWorkingMode(WorkingMode workingMode) {
		this.workingMode = workingMode;
	}
}
