package com.thefirstlineofcode.sand.server.lite.things;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.granite.framework.core.auth.IAuthenticator;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

@Transactional
@Component
public class ThingAuthenticator implements IAuthenticator {
	@Autowired
	private SqlSession sqlSession;

	@Override
	public Object getCredentials(Object principal) {
		ThingIdentity identity = getThingIdentityMapper().selectByThingName((String)principal);
		if (identity != null)
			return identity.getCredentials();
		
		return null;
	}

	@Override
	public boolean exists(Object principal) {
		return getThingIdentityMapper().selectCountByThingName((String)principal) != 0;
	}
	
	private ThingIdentityMapper getThingIdentityMapper() {
		return sqlSession.getMapper(ThingIdentityMapper.class);
	}

}
