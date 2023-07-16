package com.thefirstlineofcode.sand.protocols.sensor;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.Int2Enum;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:sensor", localName="report")
public class Report {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:sensor", "report");
	
	public enum QoS {
		AT_MOST_ONCE,
		AT_LEAST_ONCE,
		EXACTLY_ONCE
	}
	
	@Int2Enum(QoS.class)
	private QoS qos;
	
	@NotNull
	private Object data;
	
	public Report() {
		this(null);
	}
	
	public Report(Object data) {
		this(data, QoS.AT_MOST_ONCE);
	}
	
	public Report(Object data, QoS qos) {
		this.data = data;
		this.qos = qos;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}

	public QoS getQos() {
		return qos == null ? QoS.AT_MOST_ONCE : qos;
	}

	public void setQos(QoS qos) {
		this.qos = qos;
	}
}
