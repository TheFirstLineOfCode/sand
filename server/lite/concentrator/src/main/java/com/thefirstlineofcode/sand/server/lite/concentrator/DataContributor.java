package com.thefirstlineofcode.sand.server.lite.concentrator;

import org.pf4j.Extension;

import com.thefirstlineofcode.granite.framework.adf.mybatis.DataContributorAdapter;

@Extension
public class DataContributor extends DataContributorAdapter {
	@Override
	protected Class<?>[] getDataObjects() {
		return new Class<?>[] {
			D_NodeConfirmation.class,
			D_Concentration.class
		};
	}
	
	@Override
	protected String[] getInitScriptFileNames() {
		return new String[] {"concentrator.sql"};
	}
	
	@Override
	protected String[] getMapperFileNames() {
		return new String[] {
			"ConcentrationMapper.xml",
			"NodeConfirmationMapper.xml"
		};
	}
}
