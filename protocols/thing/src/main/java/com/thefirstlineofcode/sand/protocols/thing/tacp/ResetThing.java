package com.thefirstlineofcode.sand.protocols.thing.tacp;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:thing", localName="reset-thing")
public class ResetThing {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:thing", "reset-thing");
}
