package com.thefirstlineofcode.sand.client.lora.dac;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.protocols.lora.dac.ResetLoraDacService;

public class ResetLoraDacServiceExecutor  implements IExecutor<ResetLoraDacService> {
	private ILoraDacService<?> loraDacService;
	
	public ResetLoraDacServiceExecutor(ILoraDacService<?> loraDacService) {
		this.loraDacService = loraDacService;
	}
	
	@Override
	public Object execute(Iq iq, ResetLoraDacService action) throws ProtocolException {
		if (!loraDacService.isStarted())
			throw new ProtocolException(new UnexpectedRequest("DAC service doesn't start."));
		
		loraDacService.reset();
		return null;
	}

}
