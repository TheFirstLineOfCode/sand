package com.thefirstlineofcode.sand.server.lite.concentrator;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmation;

public class D_NodeConfirmation extends NodeConfirmation implements IIdProvider<String> {
	private String id;
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}
}
