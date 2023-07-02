package com.thefirstlineofcode.sand.client.friends;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;

public class FollowService implements IFollowService, IIqListener {
	private IChatServices chatServices;
	
	private IFollowProcessor followProcessor;
	
	private boolean started;
	
	public FollowService() {
		started = false;
	}
	
	@Override
	public void registerFollowedEvent(Protocol protocol, Class<?> eventType) {
		chatServices.getStream().getOxmFactory().register(
				new IqProtocolChain(Notification.PROTOCOL).next(protocol),
				new CocParserFactory<>(eventType));
	}

	@Override
	public void setFollowProcessor(IFollowProcessor followProcessor) {
		this.followProcessor = followProcessor;
	}

	@Override
	public void start() {
		if (followProcessor == null)
			throw new IllegalStateException("Null follow processor. Please call setFollowProcessor first.");
		
		chatServices.getIqService().addListener(Notification.PROTOCOL, this);
		
		started = true;
	}

	@Override
	public void stop() {
		chatServices.getIqService().removeListener(this);
		
		started = false;
	}

	@Override
	public void received(Iq iq) {
		JabberId to = iq.getTo();
		if (to != null && to.getResource() != null &&
				!to.getResource().equals(RegisteredThing.DEFAULT_RESOURCE_NAME))
			return;
		
		Notification notification = iq.getObject();
		followProcessor.process(iq.getFrom(), notification.getEvent());
	}

	@Override
	public boolean isStarted() {
		return started;
	}	
}
