package com.thefirstlineofcode.sand.demo.protocols;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "authorized-edge-things")
public class AuthorizedEdgeThings {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "authorized-edge-things");
	
	@Array(value=AuthorizedEdgeThing.class, elementName = "thing")
	private List<AuthorizedEdgeThing> things;
	
	public AuthorizedEdgeThings() {}
	
	public AuthorizedEdgeThings(List<AuthorizedEdgeThing> things) {
		this.things = things;
	}

	public List<AuthorizedEdgeThing> getThings() {
		return things;
	}

	public void setThings(List<AuthorizedEdgeThing> things) {
		this.things = things;
	}
}
