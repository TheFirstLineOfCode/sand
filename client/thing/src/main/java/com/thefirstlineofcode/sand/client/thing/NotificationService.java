package com.thefirstlineofcode.sand.client.thing;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;

public class NotificationService implements INotificationService, IIqListener {
	private IChatServices chatServices;
	private INotifier notifier;
	private Map<Class<?>, IEventProcessor<?>> eventToProcessors;
	
	public NotificationService(IChatServices chatServices) {
		this.chatServices = chatServices;
		
		eventToProcessors = new HashMap<>();
		
		chatServices.getIqService().addListener(Notification.PROTOCOL, this);
	}
	
	@Override
	public <T> void listen(Class<T> eventType, IEventProcessor<T> eventProcessor) {
		ProtocolObject protocolObject = eventType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null)
			throw new IllegalArgumentException("Isn't event type a protocol object?");
		
		Protocol protocol = new Protocol(protocolObject.namespace(), protocolObject.localName());
		chatServices.getOxmFactory().register(new IqProtocolChain(Notification.PROTOCOL).next(protocol),
				new CocParserFactory<>(eventType));
		
		eventToProcessors.put(eventType, eventProcessor);
	}

	@Override
	public INotifier getNotifier() {
		if (notifier != null)
			return notifier;
		
		notifier = new Notifier(chatServices);
		return notifier;
	}

	@Override
	public void received(Iq iq) {
		Notification notification = iq.getObject();
		Object event = notification.getEvent();
		
		IEventProcessor<?> eventProcessor = eventToProcessors.get(event.getClass());
		if (eventProcessor != null)
			processEventByProcessor(iq.getFrom(), event, eventProcessor);
	}

	@SuppressWarnings("unchecked")
	private <T> void processEventByProcessor(JabberId from, Object event, IEventProcessor<?> eventProcessor) {
		((IEventProcessor<T>)eventProcessor).processEvent(from, (T)event);
	}

}
