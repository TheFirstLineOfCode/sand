package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "deliver-temperature-to-owner")
public class DeliverTemperatureToOwner {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "deliver-temperature-to-owner");
	
	private boolean enabled;
	
	public DeliverTemperatureToOwner() {
		this(false);
	}
	
	public DeliverTemperatureToOwner(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
