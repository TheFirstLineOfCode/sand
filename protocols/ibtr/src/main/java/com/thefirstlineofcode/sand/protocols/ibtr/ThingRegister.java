package com.thefirstlineofcode.sand.protocols.ibtr;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

@ProtocolObject(namespace="urn:leps:tacp:ibtr", localName="query")
public class ThingRegister {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:ibtr", "query");
	
	private Object register;
	
	public ThingRegister() {}
	
	public ThingRegister(Object register) {
		if (!(register instanceof String) && !(register instanceof ThingIdentity))
			throw new IllegalArgumentException("Register object must be a string or a thing identity.");
		
		this.register = register;
	}

	public void setRegister(Object register) {
		this.register = register;
	}
	
	public Object getRegister() {
		return register;
	}
}
