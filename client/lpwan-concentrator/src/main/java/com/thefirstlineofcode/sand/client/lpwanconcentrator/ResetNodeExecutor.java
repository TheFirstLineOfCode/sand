package com.thefirstlineofcode.sand.client.lpwanconcentrator;

import com.thefirstlinelinecode.sand.protocols.concentrator.ResetNode;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ResetThing;

public class ResetNodeExecutor implements IExecutor<ResetNode> {
	private IConcentrator concentrator;
	
	public ResetNodeExecutor(IConcentrator concentrator) {
		this.concentrator = concentrator;
	}
	

	@Override
	public Object execute(Iq iq, ResetNode resetNode) throws ProtocolException {
		Execution execution = iq.getObject();
		Iq resetThing = new Iq(Iq.Type.SET, new Execution(new ResetThing(),
				execution.isLanTraceable(), execution.getLanTimeout()));
		resetThing.setFrom(iq.getFrom());
		resetThing.setTo(getLanNodeJid(iq, resetNode));
		
		((IIqListener)concentrator).received(resetThing);
		
		return null;
	}


	private JabberId getLanNodeJid(Iq iq, ResetNode resetNode) {
		return new JabberId(iq.getTo().getNode(), iq.getTo().getDomain(),
				resetNode.getLanId());
	}

}
