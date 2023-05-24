package com.thefirstlineofcode.sand.protocols.sensor.oxm;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Attributes;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.sensor.LanReport;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ITraceId;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public class LanReportTranslatorFactory implements ITranslatorFactory<LanReport> {

	@Override
	public Class<LanReport> getType() {
		return LanReport.class;
	}

	@Override
	public ITranslator<LanReport> create() {
		return new LanReportTranslator();
	}
	
	private class LanReportTranslator implements ITranslator<LanReport> {
		private static final String ATTRIBUTE_NAME_TRACE_ID = "trace-id";
		private static final String ATTRIBUTE_NAME_ACK_REQUIRED = "ack-required";

		@Override
		public Protocol getProtocol() {
			return LanReport.PROTOCOL;
		}

		@Override
		public String translate(LanReport lanReport, IProtocolWriter writer, ITranslatingFactory translatingFactory) {
			if (lanReport.getTraceId() == null)
				throw new IllegalArgumentException("Null trace ID.");
			
			if (lanReport.getData() == null) {
				throw new IllegalArgumentException("Null data object not be allowed in LAN report.");
			}
			
			ITraceId.Type type = ThingsTinyId.createInstance(lanReport.getTraceId()).getType();
			if (type != ITraceId.Type.REQUEST)
				throw new IllegalArgumentException("Trace ID of LAN report must be request type.");
			
			writer.writeProtocolBegin(LanReport.PROTOCOL);
			writer.writeAttributes(new Attributes().
					add(new Attribute(ATTRIBUTE_NAME_TRACE_ID, lanReport.getTraceId())).
					get());
			
			if (lanReport.isAckRequired()) {
				writer.writeAttributes(new Attributes().
						add(new Attribute(ATTRIBUTE_NAME_ACK_REQUIRED, lanReport.isAckRequired())).
						get());
			}
			
			ProtocolObject protocolObject = lanReport.getData().getClass().getAnnotation(ProtocolObject.class);
			if (protocolObject == null)
				throw new IllegalArgumentException("Data object must be an protocol object.");
			
			writer.writeString(translatingFactory.translate(lanReport.getData()));
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
	}

}
