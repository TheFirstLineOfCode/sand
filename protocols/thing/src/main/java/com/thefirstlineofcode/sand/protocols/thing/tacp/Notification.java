package com.thefirstlineofcode.sand.protocols.thing.tacp;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:thing", localName="notification")
public class Notification {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:thing", "notification");
	
	@NotNull
	private Object event;
	private boolean ackRequired;
	
	public Notification() {
		this(null);
	}
	
	public Notification(Object event) {
		this(event, false);
	}
	
	public Notification(Object event, boolean ackRequired) {
		this.event = event;
		this.ackRequired = ackRequired;
	}
	
	public Object getEvent() {
		return event;
	}
	
	public void setEvent(Object event) {
		this.event = event;
	}

	public boolean isAckRequired() {
		return ackRequired;
	}

	public void setAckRequired(boolean ackRequired) {
		this.ackRequired = ackRequired;
	}
}
