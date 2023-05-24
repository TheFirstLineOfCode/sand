package com.thefirstlineofcode.sand.protocols.actuator.oxm;

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
import com.thefirstlineofcode.sand.protocols.actuator.Execution;

public class ExecutionParserFactory implements IParserFactory<Execution> {
	private static final String ATTRIBUTE_NAME_LAN_TRACEABLE = "lan-traceable";
	private static final String ATTRIBUTE_NAME_LAN_TIMEOUT = "lan-timeout";
	
	@Override
	public Protocol getProtocol() {
		return Execution.PROTOCOL;
	}

	@Override
	public IParser<Execution> create() {
		return new ExecutionParser();
	}
	
	private class ExecutionParser implements IParser<Execution> {
		@Override
		public Execution createObject() {
			return new Execution();
		}

		@Override
		public IElementParser<Execution> getElementParser(IParsingPath parsingPath) {
			if (parsingPath.match("/")) {
				return new ElementParserAdaptor<Execution>() {
					@Override
					public void processAttributes(IParsingContext<Execution> context, List<Attribute> attributes) {
						if (attributes.size() == 0) {
							return;
						}
						
						for (Attribute attribute : attributes) {
							if (ATTRIBUTE_NAME_LAN_TRACEABLE.equals(attribute.getName())) {							
								boolean lanTraceable = Boolean.valueOf(attribute.getValue().stringIt().get());							
								context.getObject().setLanTraceable(lanTraceable);
							} else if (ATTRIBUTE_NAME_LAN_TIMEOUT.equals(attribute.getName())) {
								Integer timeout = Integer.valueOf(attribute.getValue().stringIt().get());
								context.getObject().setLanTimeout(timeout);
							} else {
								throw new ProtocolException(new BadRequest("Only optional attributes 'lan-traceable' and 'lan-timeout' are allowed in Execution."));		
							}
						}
					}
				};
			} else {
				throw new ProtocolException(new BadRequest(String.format("An invalid element found: '%s'.", parsingPath.toString())));
			}
		}

		@Override
		public void processEmbeddedObject(IParsingContext<Execution> context, Protocol protocol, Object embedded) {
			context.getObject().setAction(embedded);
		}
		
	}

}
