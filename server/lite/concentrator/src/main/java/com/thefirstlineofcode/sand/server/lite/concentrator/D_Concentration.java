package com.thefirstlineofcode.sand.server.lite.concentrator;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;
import com.thefirstlineofcode.sand.server.concentrator.Concentration;

public class D_Concentration extends Concentration implements IIdProvider<String> {
	private String id;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
