package com.thefirstlineofcode.sand.protocols.thing.tacp.oxm;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Attributes;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ITraceId;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanNotification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public class LanNotificationTranslatorFactory implements ITranslatorFactory<LanNotification> {

	@Override
	public Class<LanNotification> getType() {
		return LanNotification.class;
	}

	@Override
	public ITranslator<LanNotification> create() {
		return new LanNotificationTranslator();
	}
	
	private class LanNotificationTranslator implements ITranslator<LanNotification> {
		private static final String ATTRIBUTE_NAME_TRACE_ID = "trace-id";
		private static final String ATTRIBUTE_NAME_ACK_REQUIRED = "ack-required";

		@Override
		public Protocol getProtocol() {
			return LanNotification.PROTOCOL;
		}

		@Override
		public String translate(LanNotification lanNotification, IProtocolWriter writer, ITranslatingFactory translatingFactory) {
			if (lanNotification.getTraceId() == null)
				throw new IllegalArgumentException("Null trace ID.");
			
			if (lanNotification.getEvent() == null) {
				throw new IllegalArgumentException("Null event object not be allowed in LAN notification.");
			}
			
			ITraceId.Type type = ThingsTinyId.createInstance(lanNotification.getTraceId()).getType();
			if (type != ITraceId.Type.REQUEST)
				throw new IllegalArgumentException("Trace ID of LAN notification must be request type.");
			
			writer.writeProtocolBegin(LanNotification.PROTOCOL);
			writer.writeAttributes(new Attributes().
					add(new Attribute(ATTRIBUTE_NAME_TRACE_ID, lanNotification.getTraceId())).
					get());
			
			if (lanNotification.isAckRequired()) {
				writer.writeAttributes(new Attributes().
						add(new Attribute(ATTRIBUTE_NAME_ACK_REQUIRED, lanNotification.isAckRequired())).
						get());
			}
			
			ProtocolObject protocolObject = lanNotification.getEvent().getClass().getAnnotation(ProtocolObject.class);
			if (protocolObject == null)
				throw new IllegalArgumentException("Event object must be an protocol object.");
			
			writer.writeString(translatingFactory.translate(lanNotification.getEvent()));
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
	}

}
