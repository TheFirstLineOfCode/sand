package com.thefirstlineofcode.sand.server.lite.things;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.granite.framework.core.auth.IAuthenticator;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;

@Transactional
@Component
public class ThingAuthenticator implements IAuthenticator {
	@Autowired
	private SqlSession sqlSession;

	@Override
	public Object getCredentials(Object principal) {
		RegisteredEdgeThing registeredEdgeThing = getThingIdentityMapper().selectByThingName((String)principal);
		if (registeredEdgeThing != null)
			return registeredEdgeThing.getCredentials();
		
		return null;
	}

	@Override
	public boolean exists(Object principal) {
		return getThingIdentityMapper().selectCountByThingName((String)principal) != 0;
	}
	
	private RegisteredEdgeThingMapper getThingIdentityMapper() {
		return sqlSession.getMapper(RegisteredEdgeThingMapper.class);
	}

}
