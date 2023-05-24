package com.thefirstlineofcode.sand.client.edge;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UndefinedCondition;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.thing.IThing;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.edge.ShutdownSystem;

public class ShutdownSystemExecutor implements IExecutor<ShutdownSystem> {
	private IThing thing;
	
	public ShutdownSystemExecutor(IThing thing) {
		this.thing = thing;
	}

	@Override
	public Object execute(Iq iq, ShutdownSystem shutdownSystem) throws ProtocolException {
		try {
			thing.shutdownSystem(shutdownSystem.isRestart());
			return null;
		} catch (ExecutionException e) {
			throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
					ThingsUtils.getExecutionErrorDescription(null, e.getErrorNumber())));
		}
	}

}
