package com.thefirstlinelinecode.sand.protocols.concentrator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:concentrator", localName="reset-node")
public class ResetNode {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:concentrator", "reset-node");
	
	@NotNull
	private String lanId;
	
	public ResetNode() {}
	
	public ResetNode(String lanId) {
		this.lanId = lanId;
	}

	public String getLanId() {
		return lanId;
	}

	public void setLanId(String lanId) {
		this.lanId = lanId;
	}
}
