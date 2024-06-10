package com.thefirstlineofcode.sand.client.lpwanconcentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlinelinecode.sand.protocols.concentrator.SyncNodes;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator.SyncNodesListener;

public class SyncNodesExecutor implements IExecutor<SyncNodes> {
	private static final Logger logger = LoggerFactory.getLogger(SyncNodesExecutor.class);
	
	private IChatServices chatServices;
	private IConcentrator concentrator;
	
	public SyncNodesExecutor(IChatServices chatServices, IConcentrator concentrator) {
		this.chatServices = chatServices;
		this.concentrator = concentrator;
	}

	@Override
	public Object execute(final Iq iq, SyncNodes action) throws ProtocolException {
		concentrator.syncNodesWithServer(new SyncNodesListener() {
			
			@Override
			public void occurred(StanzaError error) {
				if (logger.isErrorEnabled())
					logger.error("Failed to sync nodes.", new ProtocolException(error));
				
				error.setId(iq.getId());
				error.setFrom(chatServices.getStream().getJid());
				error.setTo(iq.getFrom());
				
				chatServices.getErrorService().send(error);
			}
			
			@Override
			public void nodesSynced() {
				chatServices.getIqService().send(Iq.createResult(iq));
				
				if (logger.isInfoEnabled())
					logger.info("Nodes has synced.");
			}
		});
		
		return null;
	}

}
