package com.thefirstlineofcode.sand.protocols.edge;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:edge", localName="restart")
public class Restart {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:edge", "restart");
}
