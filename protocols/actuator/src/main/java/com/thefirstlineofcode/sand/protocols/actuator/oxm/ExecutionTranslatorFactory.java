package com.thefirstlineofcode.sand.protocols.actuator.oxm;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Attributes;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;

public class ExecutionTranslatorFactory implements ITranslatorFactory<Execution> {
	private static final String ATTRIBUTE_NAME_LAN_TRACEABLE = "lan-traceable";
	private static final String ATTRIBUTE_NAME_LAN_TIMEOUT = "lan-timeout";
	
	private static final ITranslator<Execution> translator = new ExecutionTranslator();

	@Override
	public Class<Execution> getType() {
		return Execution.class;
	}

	@Override
	public ITranslator<Execution> create() {
		return translator;
	}
	
	private static class ExecutionTranslator implements ITranslator<Execution> {
		@Override
		public Protocol getProtocol() {
			return Execution.PROTOCOL;
		}

		@Override
		public String translate(Execution execution, IProtocolWriter writer, ITranslatingFactory translatingFactory) {
			if (execution.getAction() == null) {
				throw new ProtocolException(new BadRequest("Null action."));
			}
			
			writer.writeProtocolBegin(Execution.PROTOCOL);
			
			if (execution.isLanTraceable()) {				
				writer.writeAttributes(new Attributes().add(new Attribute(ATTRIBUTE_NAME_LAN_TRACEABLE,
						execution.isLanTraceable())).get());
			}
			if (execution.getLanTimeout() != null)
				writer.writeAttributes(new Attributes().add(new Attribute(ATTRIBUTE_NAME_LAN_TIMEOUT,
						execution.getLanTimeout())).get());
			
			writer.writeString(translatingFactory.translate(execution.getAction()));
			
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
	}

}
