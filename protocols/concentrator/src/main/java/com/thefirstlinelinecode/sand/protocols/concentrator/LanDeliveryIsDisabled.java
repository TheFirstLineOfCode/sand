package com.thefirstlinelinecode.sand.protocols.concentrator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:concentrator", localName="lan-delivery-is-disabled")
public class LanDeliveryIsDisabled {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:concentrator", "lan-delivery-is-disabled");
}
