package com.thefirstlineofcode.sand.server.lite.friends;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.server.friends.Follow;
import com.thefirstlineofcode.sand.server.friends.FollowApproval;

public interface FollowMapper {
	void insert(FollowApproval approval);
	int selectCountByFollow(Follow follow);
	List<JabberId> selectFollowersByFriendAndEvent(JabberId friend, Protocol event);
	List<Follow> getLanFollowsByConcentrator(String concentratorThingName);
}
