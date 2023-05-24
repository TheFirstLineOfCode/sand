package com.thefirstlineofcode.sand.client.lora.gateway;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.protocols.lora.gateway.ChangeWorkingMode;

public class ChangeWorkingModeExecutor implements IExecutor<ChangeWorkingMode> {
	private ILoraGateway loraGateway;
	
	public ChangeWorkingModeExecutor(ILoraGateway loraGateway) {
		this.loraGateway = loraGateway;
	}

	@Override
	public Object execute(Iq iq, ChangeWorkingMode changeWorkingMode) throws ProtocolException {
		loraGateway.setWorkingMode(changeWorkingMode.getWorkingMode());
		return null;
	}
}
