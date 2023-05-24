package com.thefirstlineofcode.sand.demo.server.lite;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactory;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;
import com.thefirstlineofcode.sand.demo.server.IAclService;

@Component
@Transactional
public class AclService implements IAclService, IDataObjectFactoryAware {
	@Autowired
	private SqlSession sqlSession;
	
	private IDataObjectFactory dataObjectFactory;

	@Override
	public void add(AccessControlEntry ace) {
		AccessControlEntry dataObject = dataObjectFactory.create(AccessControlEntry.class);
		dataObject.setUser(ace.getUser());
		dataObject.setThingId(ace.getThingId());
		dataObject.setRole(ace.getRole());
		
		getMapper().insert(dataObject);
	}

	@Override
	public void change(AccessControlEntry ace) {
		if (ace.isRemove()) {
			remove(ace.getUser(), ace.getThingId());
			return;
		}
		
		if (getRole(ace.getUser(), ace.getThingId()) != ace.getRole())
			getMapper().updateRole(ace.getUser(), ace.getThingId(), ace.getRole());
	}

	@Override
	public void remove(String user, String thingId) {
		getMapper().remove(user, thingId);
	}

	@Override
	public AccessControlList getUserAcl(String user) {
		return assembleAcl(getMapper().selectByUser(user));
		
	}

	private AccessControlList assembleAcl(List<AccessControlEntry> entries) {
		AccessControlList acl = new AccessControlList();		
		
		if (entries != null && entries.size() != 0) {
			for (AccessControlEntry entry : entries) {
				AccessControlEntry ace = createAceProtocolObject(entry);
				acl.add(ace);
			}			
		}
		
		return acl;
	}

	private AccessControlEntry createAceProtocolObject(AccessControlEntry dataObject) {
		return new AccessControlEntry(dataObject.getUser(), dataObject.getThingId(), dataObject.getRole());
	}
	
	@Override
	public AccessControlList getOwnerAcl(String thingId) {
		return assembleAcl(getMapper().selectByThingId(thingId));
	}
	
	@Override
	public Role getRole(String user, String thingId) {
		return getMapper().selectRoleByUserAndThingId(user, thingId);
	}

	@Override
	public boolean isOwner(String user, String thingId) {
		return getRole(user, thingId) != Role.OWNER;
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}
	
	private AccessControlEntryMapper getMapper() {
		return sqlSession.getMapper(AccessControlEntryMapper.class);
	}

	@Override
	public String getOwner(String thingId) {
		return getMapper().selectOwnerByThingId(thingId);
	}

}
