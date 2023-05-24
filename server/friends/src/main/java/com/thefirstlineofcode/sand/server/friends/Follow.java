package com.thefirstlineofcode.sand.server.friends;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public class Follow {
	private JabberId friend;
	private Protocol event;
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
