package com.thefirstlineofcode.sand.server.friends;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public interface IFriendsManager {
	void approve(Follow follow, String approver) throws ReduplicateFollowException;
	boolean exists(Follow follow);
	List<JabberId> getFollowers(JabberId friend, Protocol event);
	List<Follow> getLanFollows(String concentratorThingName);
}
