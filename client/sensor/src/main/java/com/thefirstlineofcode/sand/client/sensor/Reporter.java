package com.thefirstlineofcode.sand.client.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.sand.protocols.sensor.Report;
import com.thefirstlineofcode.sand.protocols.sensor.Report.QoS;

public class Reporter implements IReporter {
	private static final Logger logger = LoggerFactory.getLogger(Reporter.class);
	
	private IChatServices chatServices;
	private QoS defaultQoS;
	private ReportCallback reportCallback;
	
	public Reporter(IChatServices chatServices) {
		this.chatServices = chatServices;
		defaultQoS = QoS.AT_MOST_ONCE;
	}

	@Override
	public void registerSupportedDatum(Class<?> datumType) {
		chatServices.getOxmFactory().register(datumType, new CocTranslatorFactory<>(datumType));
	}

	@Override
	public void setDefaultQos(QoS qos) {
		defaultQoS = qos;
	}

	@Override
	public QoS getDefaultQos() {
		return defaultQoS;
	}

	@Override
	public void setReportCallback(ReportCallback callback) {
		this.reportCallback = callback;
	}

	@Override
	public ReportCallback getReportCallback() {
		return reportCallback;
	}

	@Override
	public String report(Object data) {
		return report(null, data);
	}

	@Override
	public String report(JabberId reporter, Object data) {
		return report(reporter, data, null);
	}

	@Override
	public String report(JabberId reporter, Object data, QoS qos) {
		if (qos == null)
			qos = defaultQoS;
		
		if (qos == QoS.AT_LEAST_ONCE ||
				qos == QoS.EXACTLY_ONCE)
			throw new UnsupportedOperationException("Reporting data using QoS.AT_LEAST_ONCE and QoS.EXACTLY_ONCE policies haven't implemented yet.");
		
		reportDataUsingAtMostOncePolicy(reporter, data);
		
		return null;
	}

	private void reportDataUsingAtMostOncePolicy(JabberId reporter, Object data) {
		Report report = new Report(data, QoS.AT_MOST_ONCE);
		Iq iq = new Iq(Iq.Type.SET, report);
		if (reporter != null) {
			if (!chatServices.getStream().getJid().getBareId().equals(reporter.getBareId())) {
				throw new IllegalArgumentException(String.format("Illegal reporter JID. Sender JID: %s. Reporter JID: %s",
						chatServices.getStream().getJid(), reporter));
			}
			
			iq.setFrom(reporter);
		}
		
		if (logger.isInfoEnabled())
			logger.info("Send a report which's stanza ID is '{}' and which's object object is '{}' using QoS.AT_MOST_ONCE policy.",
						iq.getId(), report.getData(), report.getQos());
		
		chatServices.getIqService().send(iq);
		
		if (reportCallback != null)
			reportCallback.reported(iq.getId(), reporter, data);
	}
}
