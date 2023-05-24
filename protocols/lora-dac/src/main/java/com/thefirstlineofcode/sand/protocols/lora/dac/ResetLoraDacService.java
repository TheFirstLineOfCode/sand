package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:lora-dac", localName="reset-lora-dac-service")
public class ResetLoraDacService {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:lora-dac", "reset-lora-dac-service");
}
