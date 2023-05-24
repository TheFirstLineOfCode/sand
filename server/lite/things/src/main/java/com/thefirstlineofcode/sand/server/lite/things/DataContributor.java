package com.thefirstlineofcode.sand.server.lite.things;

import org.pf4j.Extension;

import com.thefirstlineofcode.granite.framework.adf.mybatis.DataContributorAdapter;

@Extension
public class DataContributor extends DataContributorAdapter {
	@Override
	protected Class<?>[] getDataObjects() {
		return new Class<?>[] {
			D_Thing.class,
			D_ThingAuthorization.class,
			D_ThingIdentity.class
		};		
	}
	
	@Override
	protected String[] getInitScriptFileNames() {
		return new String[] {"thing.sql"};
	}
	
	@Override
	protected String[] getMapperFileNames() {
		return new String[] {
			"ThingAuthorizationMapper.xml",
			"ThingIdentityMapper.xml",
			"ThingMapper.xml"
		};
	}
}
