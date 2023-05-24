package com.thefirstlineofcode.sand.client.sensor;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.sand.protocols.sensor.Report.QoS;

public interface IReporter {	
	public interface ReportCallback {
		void reported(String id, JabberId reporter, Object data);
	}
	
	void registerSupportedDatum(Class<?> datumType);
	
	void setDefaultQos(QoS qos);
	QoS getDefaultQos();
	void setReportCallback(ReportCallback callback);
	ReportCallback getReportCallback();
	
	String report(Object data);
	String report(JabberId reporter, Object data);
	String report(JabberId reporter, Object data, QoS qos);
}
