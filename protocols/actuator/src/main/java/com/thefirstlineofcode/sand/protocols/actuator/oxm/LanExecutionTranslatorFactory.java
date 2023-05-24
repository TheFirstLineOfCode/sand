package com.thefirstlineofcode.sand.protocols.actuator.oxm;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Attributes;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.actuator.LanExecution;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ITraceId;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public class LanExecutionTranslatorFactory implements ITranslatorFactory<LanExecution> {

	@Override
	public Class<LanExecution> getType() {
		return LanExecution.class;
	}

	@Override
	public ITranslator<LanExecution> create() {
		return new LanExecutionTranslator();
	}
	
	private class LanExecutionTranslator implements ITranslator<LanExecution> {
		private static final String ATTRIBUTE_NAME_TRACE_ID = "trace-id";

		@Override
		public Protocol getProtocol() {
			return LanExecution.PROTOCOL;
		}

		@Override
		public String translate(LanExecution lanExecution, IProtocolWriter writer, ITranslatingFactory translatingFactory) {
			if (lanExecution.getTraceId() == null)
				throw new IllegalArgumentException("Null trace ID.");
			
			if (lanExecution.getAction() == null) {
				throw new IllegalArgumentException("Null action object not be allowed in LAN execution.");
			}
			
			ITraceId.Type type = ThingsTinyId.createInstance(lanExecution.getTraceId()).getType();
			if (type != ITraceId.Type.REQUEST)
				throw new IllegalArgumentException("Trace ID of LAN execution must be request type.");
			
			writer.writeProtocolBegin(LanExecution.PROTOCOL);
			writer.writeAttributes(new Attributes().
					add(new Attribute(ATTRIBUTE_NAME_TRACE_ID, lanExecution.getTraceId())).
					get());
			
			ProtocolObject protocolObject = lanExecution.getAction().getClass().getAnnotation(ProtocolObject.class);
			if (protocolObject == null)
				throw new IllegalArgumentException("Action object must be an protocol object.");
			
			writer.writeString(translatingFactory.translate(lanExecution.getAction()));
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
	}

}
