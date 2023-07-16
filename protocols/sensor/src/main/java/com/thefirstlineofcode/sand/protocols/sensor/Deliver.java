package com.thefirstlineofcode.sand.protocols.sensor;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:sensor", localName="deliver")
public class Deliver {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:sensor", "deliver");
	
	@NotNull
	private String reportId;
	
	public Deliver() {}
	
	public Deliver(String reportId) {
		this.reportId = reportId;
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
}
