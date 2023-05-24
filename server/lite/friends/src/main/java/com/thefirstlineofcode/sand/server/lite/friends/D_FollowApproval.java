package com.thefirstlineofcode.sand.server.lite.friends;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;
import com.thefirstlineofcode.sand.server.friends.FollowApproval;

public class D_FollowApproval extends FollowApproval implements IIdProvider<String> {
	private String id;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
