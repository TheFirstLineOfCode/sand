package com.thefirstlineofcode.sand.protocols.things.simple.light;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.Int2Enum;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:things:simple-light", localName="switch-state-changed")
public class SwitchStateChanged {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:things:simple-light", "switch-state-changed");
	
	@Int2Enum(SwitchState.class)
	private SwitchState previous;
	
	@Int2Enum(SwitchState.class)
	private SwitchState current;
	
	public SwitchStateChanged() {
		this(null, null);
	}
	
	public SwitchStateChanged(SwitchState previous, SwitchState current) {
		this.previous = previous;
		this.current = current;
	}

	public SwitchState getPrevious() {
		return previous;
	}

	public void setPrevious(SwitchState previous) {
		this.previous = previous;
	}

	public SwitchState getCurrent() {
		return current;
	}

	public void setCurrent(SwitchState current) {
		this.current = current;
	}
	
	@Override
	public String toString() {
		return String.format("SwitchStateChanged[previous: %s, current: %s.]", previous, current);
	}
}
