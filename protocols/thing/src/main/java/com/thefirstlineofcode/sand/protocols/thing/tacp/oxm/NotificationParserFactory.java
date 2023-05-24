package com.thefirstlineofcode.sand.protocols.thing.tacp.oxm;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.parsing.ElementParserAdaptor;
import com.thefirstlineofcode.basalt.oxm.parsing.IElementParser;
import com.thefirstlineofcode.basalt.oxm.parsing.IParser;
import com.thefirstlineofcode.basalt.oxm.parsing.IParserFactory;
import com.thefirstlineofcode.basalt.oxm.parsing.IParsingContext;
import com.thefirstlineofcode.basalt.oxm.parsing.IParsingPath;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;

public class NotificationParserFactory implements IParserFactory<Notification> {
	private static final String ATTRIBUTE_NAME_ACK_REQUIRED = "ack-required";
	
	@Override
	public Protocol getProtocol() {
		return Notification.PROTOCOL;
	}

	@Override
	public IParser<Notification> create() {
		return new NotificationParser();
	}
	
	private class NotificationParser implements IParser<Notification> {
		@Override
		public Notification createObject() {
			return new Notification();
		}

		@Override
		public IElementParser<Notification> getElementParser(IParsingPath parsingPath) {
			if (parsingPath.match("/")) {
				return new ElementParserAdaptor<Notification>() {
					@Override
					public void processAttributes(IParsingContext<Notification> context, List<Attribute> attributes) {
						if (attributes.size() == 0) {
							return;
						}
						
						for (Attribute attribute : attributes) {
							if (ATTRIBUTE_NAME_ACK_REQUIRED.equals(attribute.getName())) {							
								boolean ackRequired = Boolean.valueOf(attribute.getValue().stringIt().get());								
								context.getObject().setAckRequired(ackRequired);
							} else {
								throw new ProtocolException(new BadRequest("Only optional attribute 'ack-required' is allowed in Notification."));
							}
						}
					}
				};
			} else {
				throw new ProtocolException(new BadRequest(String.format("An invalid element found: '%s'.", parsingPath.toString())));
			}
		}

		@Override
		public void processEmbeddedObject(IParsingContext<Notification> context, Protocol protocol, Object embedded) {
			context.getObject().setEvent(embedded);
		}
	}

}
