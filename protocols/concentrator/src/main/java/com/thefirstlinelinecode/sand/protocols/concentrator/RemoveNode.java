package com.thefirstlinelinecode.sand.protocols.concentrator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:concentrator", localName="remove-node")
public class RemoveNode {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:concentrator", "remove-node");
	
	@NotNull
	private Integer lanId;
	
	public RemoveNode() {}
	
	public RemoveNode(Integer lanId) {
		this.lanId = lanId;
	}

	public Integer getLanId() {
		return lanId;
	}

	public void setLanId(Integer lanId) {
		this.lanId = lanId;
	}
}
