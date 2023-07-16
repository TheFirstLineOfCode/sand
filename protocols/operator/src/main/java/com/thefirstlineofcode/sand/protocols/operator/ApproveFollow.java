package com.thefirstlineofcode.sand.protocols.operator;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.String2JabberId;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.String2Protocol;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "urn:leps:tuxp:operator", localName = "approve-follow")
public class ApproveFollow {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:operator", "approve-follow");
	
	@NotNull
	@String2JabberId
	private JabberId friend;
	
	@NotNull
	@String2Protocol
	private Protocol event;
	
	@NotNull
	@String2JabberId
	private JabberId follower;
	
	public ApproveFollow() {}
	
	public ApproveFollow(JabberId friend, Protocol event, JabberId follower) {
		this.friend = friend;
		this.event = event;
		this.follower = follower;
	}
	
	public JabberId getFriend() {
		return friend;
	}
	
	public void setFriend(JabberId friend) {
		this.friend = friend;
	}
	
	public Protocol getEvent() {
		return event;
	}
	
	public void setEvent(Protocol event) {
		this.event = event;
	
	}
	
	public JabberId getFollower() {
		return follower;
	}
	
	public void setFollower(JabberId follower) {
		this.follower = follower;
	}
	
}
