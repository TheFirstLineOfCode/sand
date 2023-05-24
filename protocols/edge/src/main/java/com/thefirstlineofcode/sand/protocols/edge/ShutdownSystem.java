package com.thefirstlineofcode.sand.protocols.edge;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.BooleanOnly;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:edge", localName="shutdown-system")
public class ShutdownSystem {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:edge", "shutdown-system");
	
	@BooleanOnly
	private boolean restart;
	
	public ShutdownSystem() {
		this(false);
	}
	
	public ShutdownSystem(boolean restart) {
		this.restart = restart;
	}

	public boolean isRestart() {
		return restart;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}
}
