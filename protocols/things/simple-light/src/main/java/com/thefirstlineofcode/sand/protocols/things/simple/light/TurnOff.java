package com.thefirstlineofcode.sand.protocols.things.simple.light;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:things:simple-light", localName="turn-off")
public class TurnOff {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:things:simple-light", "turn-off");
}
