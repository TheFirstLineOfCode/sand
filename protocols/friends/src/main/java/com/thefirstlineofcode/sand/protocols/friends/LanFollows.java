package com.thefirstlineofcode.sand.protocols.friends;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:friends", localName="lan-follows")
public class LanFollows {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:friends", "lan-follows");
	
	@Array(value=Follow.class, elementName="follow")
	private List<Follow> follows;
	
	public LanFollows() {}
	
	public LanFollows(List<Follow> follows) {
		this.follows = follows;
	}

	public List<Follow> getFollows() {
		if (follows == null)
			follows = new ArrayList<>();
		
		return follows;
	}

	public void setFollows(List<Follow> follows) {
		this.follows = follows;
	}
}
