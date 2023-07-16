package com.thefirstlineofcode.sand.protocols.ibtr;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.protocols.thing.UnregisteredThing;

@ProtocolObject(namespace="urn:leps:tuxp:ibtr", localName="thing-register")
public class ThingRegister {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:ibtr", "thing-register");
	
	private Object register;
	
	public ThingRegister() {}
	
	public ThingRegister(Object register) {
		if (!(register instanceof UnregisteredThing) && !(register instanceof RegisteredThing))
			throw new IllegalArgumentException("Register object must be a unregistered thing or a registered thing.");
		
		this.register = register;
	}

	public void setRegister(Object register) {
		this.register = register;
	}
	
	public Object getRegister() {
		return register;
	}
}
