package com.thefirstlineofcode.sand.protocols.ibtr;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stream.Feature;

@ProtocolObject(namespace="urn:leps:tacp:ibtr", localName="register")
public class Register implements Feature {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:ibtr", "register");
}
