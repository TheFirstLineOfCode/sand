package com.thefirstlineofcode.sand.protocols.actuator.oxm;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.parsing.ElementParserAdaptor;
import com.thefirstlineofcode.basalt.oxm.parsing.IElementParser;
import com.thefirstlineofcode.basalt.oxm.parsing.IParser;
import com.thefirstlineofcode.basalt.oxm.parsing.IParserFactory;
import com.thefirstlineofcode.basalt.oxm.parsing.IParsingContext;
import com.thefirstlineofcode.basalt.oxm.parsing.IParsingPath;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.sand.protocols.actuator.LanExecution;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ITraceId;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public class LanExecutionParserFactory implements IParserFactory<LanExecution> {	
	@Override
	public Protocol getProtocol() {
		return LanExecution.PROTOCOL;
	}

	@Override
	public IParser<LanExecution> create() {
		return new LanExecutionParser();
	}
	
	private class LanExecutionParser implements IParser<LanExecution> {
		private static final String ATTRIBUTE_NAME_TRACE_ID = "trace-id";
		
		@Override
		public LanExecution createObject() {
			return new LanExecution();
		}

		@Override
		public IElementParser<LanExecution> getElementParser(IParsingPath parsingPath) {
			if (parsingPath.match("/")) {
				return new ElementParserAdaptor<LanExecution>() {
					@Override
					public void processAttributes(IParsingContext<LanExecution> context, List<Attribute> attributes) {
						if (attributes.size() != 1 || !ATTRIBUTE_NAME_TRACE_ID.equals(attributes.get(0).getLocalName())) {
							throw new ProtocolException(new BadRequest("No trace ID found."));
						}
						
						String sTraceId = attributes.get(0).getValue().getString();
						if (!BinaryUtils.isBase64Encoded(sTraceId))
							throw new ProtocolException(new BadRequest("malformed trace ID."));
						
						
						byte[] traceId = BinaryUtils.unescape(BinaryUtils.decodeFromBase64(sTraceId));
						if (ThingsTinyId.createInstance(traceId).getType() != ITraceId.Type.REQUEST)
							throw new IllegalArgumentException("Trace ID of LAN execution must be request type.");
						
						context.getObject().setTraceId(traceId);
					}
				};
			} else {
				throw new ProtocolException(new BadRequest(String.format("An invalid element found: '%s'.", parsingPath.toString())));
			}
		}

		@Override
		public void processEmbeddedObject(IParsingContext<LanExecution> context, Protocol protocol, Object embedded) {				
			context.getObject().setAction(embedded);
		}
	}

}
