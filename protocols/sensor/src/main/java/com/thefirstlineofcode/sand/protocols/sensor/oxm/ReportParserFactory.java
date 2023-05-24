package com.thefirstlineofcode.sand.protocols.sensor.oxm;

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
import com.thefirstlineofcode.sand.protocols.sensor.Report;
import com.thefirstlineofcode.sand.protocols.sensor.Report.QoS;

public class ReportParserFactory implements IParserFactory<Report> {
	private static final String ATTRIBUTE_NAME_QOS = "qos";
	
	@Override
	public Protocol getProtocol() {
		return Report.PROTOCOL;
	}

	@Override
	public IParser<Report> create() {
		return new ReportParser();
	}
	
	private class ReportParser implements IParser<Report> {
		@Override
		public Report createObject() {
			return new Report();
		}

		@Override
		public IElementParser<Report> getElementParser(IParsingPath parsingPath) {
			if (parsingPath.match("/")) {
				return new ElementParserAdaptor<Report>() {
					@Override
					public void processAttributes(IParsingContext<Report> context, List<Attribute> attributes) {
						if (attributes.size() == 0) {
							return;
						}
						
						for (Attribute attribute : attributes) {
							if (ATTRIBUTE_NAME_QOS.equals(attribute.getName())) {							
								int iQos = Integer.valueOf(attribute.getValue().stringIt().get());								
								for (QoS qos : QoS.values()) {
									if (qos.ordinal() == iQos) {										
										context.getObject().setQos(qos);
										break;
									}
								}
							} else {
								throw new ProtocolException(new BadRequest("Only attribute 'qos' is allowed in Report."));						
							}
						}
					}
				};
			} else {
				throw new ProtocolException(new BadRequest(String.format("An invalid element found: '%s'.", parsingPath.toString())));
			}
		}

		@Override
		public void processEmbeddedObject(IParsingContext<Report> context, Protocol protocol, Object embedded) {
			context.getObject().setData(embedded);
		}
	}

}
