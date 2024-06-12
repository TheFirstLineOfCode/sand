package com.thefirstlinelinecode.sand.protocols.lpwan.concentrator.friends;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:concentrator", localName="lan-follows")
public class LanFollows {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:concentrator", "lan-follows");
	
	@Array(value=LanFollow.class, elementName="lan-follow")
	private List<LanFollow> lanFollows;
	
	public LanFollows() {}
	
	public LanFollows(List<LanFollow> lanFollows) {
		this.lanFollows = lanFollows;
	}

	public List<LanFollow> getLanFollows() {
		if (lanFollows == null)
			lanFollows = new ArrayList<>();
		
		return lanFollows;
	}

	public void setLanFollows(List<LanFollow> lanFollows) {
		this.lanFollows = lanFollows;
	}
}
