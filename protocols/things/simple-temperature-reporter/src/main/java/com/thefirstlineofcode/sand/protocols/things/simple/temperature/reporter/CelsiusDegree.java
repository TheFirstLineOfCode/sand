package com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:things:simple-temperature-reporter", localName="celsius-degree")
public class CelsiusDegree {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:things:simple-temperature-reporter", "celsius-degree");
	
	private float value;

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
}
