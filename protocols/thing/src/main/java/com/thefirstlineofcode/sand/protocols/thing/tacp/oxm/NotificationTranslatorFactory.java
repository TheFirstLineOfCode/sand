package com.thefirstlineofcode.sand.protocols.thing.tacp.oxm;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Attributes;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;

public class NotificationTranslatorFactory implements ITranslatorFactory<Notification> {
	private static final String ATTRIBUTE_NAME_ACK_REQUIRED = "ack-required";
	
	private static final ITranslator<Notification> translator = new NotificationTranslator();

	@Override
	public Class<Notification> getType() {
		return Notification.class;
	}

	@Override
	public ITranslator<Notification> create() {
		return translator;
	}
	
	private static class NotificationTranslator implements ITranslator<Notification> {
		@Override
		public Protocol getProtocol() {
			return Notification.PROTOCOL;
		}

		@Override
		public String translate(Notification notification, IProtocolWriter writer, ITranslatingFactory translatingFactory) {
			if (notification.getEvent() == null) {
				throw new ProtocolException(new BadRequest("Null event."));
			}
			
			writer.writeProtocolBegin(Notification.PROTOCOL);
			
			if (notification.isAckRequired()) {				
				writer.writeAttributes(new Attributes().add(new Attribute(ATTRIBUTE_NAME_ACK_REQUIRED,
						notification.isAckRequired())).get());
			}
			
			writer.writeString(translatingFactory.translate(notification.getEvent()));
			
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
	}

}
