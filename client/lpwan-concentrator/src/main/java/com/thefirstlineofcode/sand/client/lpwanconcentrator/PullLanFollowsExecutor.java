package com.thefirstlineofcode.sand.client.lpwanconcentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlinelinecode.sand.protocols.lpwan.concentrator.friends.PullLanFollows;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.lpwanconcentrator.ILpwanConcentrator.PullLanFollowsListener;

public class PullLanFollowsExecutor implements IExecutor<PullLanFollows> {
	private static final Logger logger = LoggerFactory.getLogger(PullLanFollowsExecutor.class);
	
	private IChatServices chatServices;
	private ILpwanConcentrator concentrator;
	
	public PullLanFollowsExecutor(IChatServices chatServices, ILpwanConcentrator concentrator) {
		this.chatServices = chatServices;
		this.concentrator = concentrator;
	}

	@Override
	public Object execute(final Iq iq, PullLanFollows action) throws ProtocolException {
		concentrator.pullLanFollows(new PullLanFollowsListener() {
			
			@Override
			public void occurred(StanzaError error) {
				if (logger.isErrorEnabled())
					logger.error("Failed to pull LAN follows.", new ProtocolException(error));
				
				error.setId(iq.getId());
				error.setFrom(chatServices.getStream().getJid());
				error.setTo(iq.getFrom());
				
				chatServices.getErrorService().send(error);
			}
			
			@Override
			public void lanFollowsPulled() {
				if (logger.isInfoEnabled())
					logger.info("LAN follows has pulled.");
				
				chatServices.getIqService().send(Iq.createResult(iq));
			}
		});
		
		return null;
	}

}
