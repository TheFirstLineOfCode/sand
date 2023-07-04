package com.thefirstlineofcode.sand.demo.protocols;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "authorized-things")
public class AuthorizedThings {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "authorized-things");
	
	@Array(value=AuthorizedThing.class, elementName = "thing")
	private List<AuthorizedThing> things;
	
	public AuthorizedThings() {}
	
	public AuthorizedThings(List<AuthorizedThing> things) {
		this.things = things;
	}

	public List<AuthorizedThing> getThings() {
		return things;
	}

	public void setThings(List<AuthorizedThing> things) {
		this.things = things;
	}
}
