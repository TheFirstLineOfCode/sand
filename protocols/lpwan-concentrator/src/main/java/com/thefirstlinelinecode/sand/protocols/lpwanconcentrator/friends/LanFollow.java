package com.thefirstlinelinecode.sand.protocols.lpwanconcentrator.friends;

import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.String2Protocol;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.NotNull;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public class LanFollow {
	@NotNull
	private Integer friendLanId;
	@NotNull
	@String2Protocol
	private Protocol event;
	@NotNull
	private Integer followerLanId;
	
	public LanFollow() {}
	
	public LanFollow(int friendLanId, Protocol event, int followerLanId) {
		this.friendLanId = friendLanId;
		this.event = event;
		this.followerLanId = followerLanId;
	}
	
	public int getFollowerLanId() {
		return followerLanId;
	}

	public void setFollowerLanId(int followerLanId) {
		this.followerLanId = followerLanId;
	}

	public int getFriendLanId() {
		return friendLanId;
	}
	
	public void setFriendLanId(int friendLanId) {
		this.friendLanId = friendLanId;
	}
	
	public Protocol getEvent() {
		return event;
	}
	
	public void setEvent(Protocol event) {
		this.event = event;
	}
	
}
