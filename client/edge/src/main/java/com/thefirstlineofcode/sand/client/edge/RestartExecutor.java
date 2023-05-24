package com.thefirstlineofcode.sand.client.edge;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UndefinedCondition;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.thing.IThing;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.edge.Restart;

public class RestartExecutor implements IExecutor<Restart> {
private IThing thing;
	
	public RestartExecutor(IThing thing) {
		this.thing = thing;
	}

	@Override
	public Object execute(Iq iq, Restart restart) throws ProtocolException {
		try {
			thing.restart();
			return null;
		} catch (ExecutionException e) {
			throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
					ThingsUtils.getExecutionErrorDescription(null, e.getErrorNumber())));
		}
	}
}
