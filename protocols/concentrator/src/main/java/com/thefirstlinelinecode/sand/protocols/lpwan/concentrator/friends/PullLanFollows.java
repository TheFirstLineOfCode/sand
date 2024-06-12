package com.thefirstlinelinecode.sand.protocols.lpwan.concentrator.friends;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:concentrator", localName="pull-lan-follows")
public class PullLanFollows {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:concentrator", "pull-lan-follows");
	
}
