package com.thefirstlineofcode.sand.protocols.friends;

import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.String2JabberId;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public class Follow {
	@NotNull
	@String2JabberId
	private JabberId friend;
	@NotNull
	private Protocol event;
	@NotNull
	@String2JabberId
	private JabberId follower;
	
	public Follow() {}
	
	public Follow(JabberId friend, Protocol event, JabberId follower) {
		this.friend = friend;
		this.event = event;
		this.follower = follower;
	}
	
	public JabberId getFollower() {
		return follower;
	}

	public void setFollower(JabberId follower) {
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
	
}
