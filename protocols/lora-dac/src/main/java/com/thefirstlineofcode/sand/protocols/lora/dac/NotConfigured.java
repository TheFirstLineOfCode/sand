package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:lora-dac", localName="not-configured")
public class NotConfigured {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:lora-dac", "not-configured");
}
