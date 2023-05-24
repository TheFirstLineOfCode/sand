package com.thefirstlineofcode.sand.protocols.sensor.oxm;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Attributes;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.sand.protocols.sensor.Report;

public class ReportTranslatorFactory implements ITranslatorFactory<Report> {
	private static final String ATTRIBUTE_NAME_QOS = "qos";
	
	private static final ITranslator<Report> translator = new ReportTranslator();

	@Override
	public Class<Report> getType() {
		return Report.class;
	}

	@Override
	public ITranslator<Report> create() {
		return translator;
	}
	
	private static class ReportTranslator implements ITranslator<Report> {
		@Override
		public Protocol getProtocol() {
			return Report.PROTOCOL;
		}

		@Override
		public String translate(Report report, IProtocolWriter writer, ITranslatingFactory translatingFactory) {
			if (report.getData() == null) {
				throw new ProtocolException(new BadRequest("Null data."));
			}
			
			writer.writeProtocolBegin(Report.PROTOCOL);
			
			writer.writeAttributes(new Attributes().add(new Attribute(ATTRIBUTE_NAME_QOS,
					report.getQos().ordinal())).get());
			
			writer.writeString(translatingFactory.translate(report.getData()));
			
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
	}

}
