package com.thefirstlineofcode.sand.protocols.sensor.oxm;

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
import com.thefirstlineofcode.sand.protocols.sensor.LanReport;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ITraceId;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public class LanReportParserFactory implements IParserFactory<LanReport> {	
	@Override
	public Protocol getProtocol() {
		return LanReport.PROTOCOL;
	}

	@Override
	public IParser<LanReport> create() {
		return new LanReportParser();
	}
	
	private class LanReportParser implements IParser<LanReport> {
		private static final String ATTRIBUTE_NAME_TRACE_ID = "trace-id";
		private static final String ATTRIBUTE_NAME_ACK_REQUIRED = "ack-required";
		
		@Override
		public LanReport createObject() {
			return new LanReport();
		}

		@Override
		public IElementParser<LanReport> getElementParser(IParsingPath parsingPath) {
			if (parsingPath.match("/")) {
				return new ElementParserAdaptor<LanReport>() {
					@Override
					public void processAttributes(IParsingContext<LanReport> context, List<Attribute> attributes) {
						if (attributes.size() < 1) {
							throw new ProtocolException(new BadRequest("Trace ID attribute is needed at least."));
						}
						
						for (Attribute attribute : attributes) {
							if (ATTRIBUTE_NAME_TRACE_ID.equals(attribute.getLocalName())) {
								String sTraceId = attribute.getValue().getString();
								if (!BinaryUtils.isBase64Encoded(sTraceId))
									throw new ProtocolException(new BadRequest("malformed trace ID."));
								
								byte[] traceId = BinaryUtils.unescape(BinaryUtils.decodeFromBase64(sTraceId));
								if (ThingsTinyId.createInstance(traceId).getType() != ITraceId.Type.REQUEST)
									throw new IllegalArgumentException("Trace ID of LAN report must be request type.");
								
								context.getObject().setTraceId(traceId);								
							} else if (ATTRIBUTE_NAME_ACK_REQUIRED.equals(attribute.getLocalName())) {
								boolean ackRequired = Boolean.valueOf(attribute.getValue().stringIt().get());
								context.getObject().setAckRequired(ackRequired);
							} else {
								throw new ProtocolException(new BadRequest(String.format("An invalid attribute found: '%s'.", attribute.getLocalName())));
							}
						}
					}
				};
			} else {
				throw new ProtocolException(new BadRequest(String.format("An invalid element found: '%s'.", parsingPath.toString())));
			}
		}

		@Override
		public void processEmbeddedObject(IParsingContext<LanReport> context, Protocol protocol, Object embedded) {				
			context.getObject().setData(embedded);
		}
	}

}
