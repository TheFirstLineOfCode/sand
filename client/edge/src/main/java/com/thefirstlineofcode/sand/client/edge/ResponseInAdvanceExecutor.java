package com.thefirstlineofcode.sand.client.edge;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.thing.IThing;

public class ResponseInAdvanceExecutor<T> implements IExecutor<T> {
	private IExecutor<T> original;
	private IThing thing;
	
	public ResponseInAdvanceExecutor(IExecutor<T> original, IThing thing) {
		this.original = original;
		this.thing = thing;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(Iq iq, Object action) throws ProtocolException {
		if (thing instanceof IEdgeThing) {
			IEdgeThing edge = (IEdgeThing)thing;
			IChatClient chatClient = edge.getChatClient();
			
			Iq result = new Iq(Iq.Type.RESULT, iq.getId());
			setFromToAddresses(iq.getFrom(), iq.getTo(), result);
			chatClient.getChatServices().getIqService().send(result);
		}
		
		return original.execute(iq, (T)action);
	}
	
	private void setFromToAddresses(JabberId from, JabberId to, Stanza stanza) {
		stanza.setFrom(to.getBareId());
		
		if (from != null && !((IEdgeThing)thing).getStreamConfig().getHost().equals(from.toString())) {
			stanza.setTo(from);
		}
	}
	
}

