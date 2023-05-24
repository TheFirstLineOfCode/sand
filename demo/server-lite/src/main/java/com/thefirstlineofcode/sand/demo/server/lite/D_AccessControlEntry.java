package com.thefirstlineofcode.sand.demo.server.lite;

import java.util.Date;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;

public class D_AccessControlEntry extends AccessControlEntry implements IIdProvider<String> {
	private String id;
	private Date updateTime;
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
