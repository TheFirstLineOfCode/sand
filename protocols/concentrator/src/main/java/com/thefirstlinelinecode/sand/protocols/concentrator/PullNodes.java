package com.thefirstlinelinecode.sand.protocols.concentrator;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:concentrator", localName="pull-nodes")
public class PullNodes {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:concentrator", "pull-nodes");
	
	@Array(value = Node.class, elementName = "node")
	private List<Node> nodes;
	
	public PullNodes() {}
	
	public PullNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
}
