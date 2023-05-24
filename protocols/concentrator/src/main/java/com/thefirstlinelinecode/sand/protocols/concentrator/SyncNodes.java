package com.thefirstlinelinecode.sand.protocols.concentrator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:concentrator", localName="sync-nodes")
public class SyncNodes {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:concentrator", "sync-nodes");
}
