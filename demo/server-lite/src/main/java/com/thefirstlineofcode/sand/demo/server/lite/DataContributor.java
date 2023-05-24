package com.thefirstlineofcode.sand.demo.server.lite;

import org.pf4j.Extension;

import com.thefirstlineofcode.granite.framework.adf.mybatis.DataContributorAdapter;
import com.thefirstlineofcode.granite.framework.adf.mybatis.TypeHandlerMapping;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded.RecordingReason;

@Extension
public class DataContributor extends DataContributorAdapter {
	@Override
	protected Class<?>[] getDataObjects() {
		return new Class<?>[] {
			D_AccessControlEntry.class,
			D_RecordedVideo.class
		};
	}
	
	@Override
	protected String[] getInitScriptFileNames() {
		return new String[] {
			"sand-demo.sql"	
		};
	}
	
	@Override
	protected String[] getMapperFileNames() {
		return new String[] {
			"AccessControlEntryMapper.xml",
			"RecordedVideoMapper.xml"
		};
	}
	
	@Override
	public TypeHandlerMapping[] getTypeHandlerMappings() {
		return new TypeHandlerMapping[] {
			new TypeHandlerMapping(Role.class, RoleTypeHandler.class),
			new TypeHandlerMapping(RecordingReason.class, RecordingReasonTypeHandler.class)
		};
	}
}
