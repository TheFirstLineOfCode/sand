package com.thefirstlineofcode.sand.protocols.actuator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:actuator", localName="execution")
public class Execution {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:actuator", "execution");
	
	@NotNull
	private Object action;
	private boolean lanTraceable;
	private Integer lanTimeout;
	
	public Execution() {
		lanTraceable = false;
	}
	
	public Execution(Object action) {
		this(action, false);
	}
	
	public Execution(Object action, boolean lanTraceable) {
		this(action, lanTraceable, null);
	}
	
	public Execution(Object action, boolean lanTraceable, Integer lanTimeout) {
		this.action = action;
		this.lanTraceable = lanTraceable;
		this.lanTimeout = lanTimeout;
	}
	
	public Object getAction() {
		return action;
	}
	
	public void setAction(Object action) {
		this.action = action;
	}

	public boolean isLanTraceable() {
		return lanTraceable;
	}

	public void setLanTraceable(boolean lanTraceable) {
		this.lanTraceable = lanTraceable;
	}

	public Integer getLanTimeout() {
		return lanTimeout;
	}

	public void setLanTimeout(Integer lanTimeout) {
		this.lanTimeout = lanTimeout;
	}
	
}
