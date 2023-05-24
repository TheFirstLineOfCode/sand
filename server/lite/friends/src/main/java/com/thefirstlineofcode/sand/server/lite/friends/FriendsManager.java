package com.thefirstlineofcode.sand.server.lite.friends;

import java.util.Calendar;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Forbidden;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactory;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.sand.server.friends.Follow;
import com.thefirstlineofcode.sand.server.friends.FollowApproval;
import com.thefirstlineofcode.sand.server.friends.IFriendsManager;
import com.thefirstlineofcode.sand.server.friends.ReduplicateFollowException;

@Component
@Transactional
public class FriendsManager implements IFriendsManager, IDataObjectFactoryAware {
	@Autowired
	private SqlSession sqlSession;
	
	@Autowired
	private IAccountManager accountManager;
	
	private IDataObjectFactory dataObjectFactory;

	@Override
	public void approve(Follow follow, String approver) throws ReduplicateFollowException {
		if (follow.getFriend() == null || follow.getFollower() == null)
			throw new IllegalArgumentException("Null friend or follower.");
		
		if (follow.getEvent() == null)
			throw new IllegalArgumentException("Null event.");
		
		if (approver == null)
			throw new IllegalArgumentException("Null approver.");
		
		if (!accountManager.exists(approver))
			throw new ProtocolException(new Forbidden(String.format("'%s' isn't a valid user.", approver)));
		
		if (exists(follow))
			throw new ReduplicateFollowException();
		
		FollowApproval approval = dataObjectFactory.create(FollowApproval.class);
		approval.setFollow(follow);
		approval.setApprover(approver);
		approval.setApprovalTime(Calendar.getInstance().getTime());
		
		getFollowMapper().insert(approval);
	}
	
	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}
	
	private FollowMapper getFollowMapper() {
		return sqlSession.getMapper(FollowMapper.class);
	}

	@Override
	public boolean exists(Follow follow) {
		return getFollowMapper().selectCountByFollow(follow) != 0;
	}

	@Override
	public List<JabberId> getFollowers(JabberId friend, Protocol event) {
		return getFollowMapper().selectFollowersByFriendAndEvent(friend, event);
	}

	@Override
	public List<Follow> getLanFollows(String concentratorThingName) {
		return getFollowMapper().getLanFollowsByConcentrator(concentratorThingName);
	}

}
