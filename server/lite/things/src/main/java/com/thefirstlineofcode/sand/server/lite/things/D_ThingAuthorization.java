package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;
import com.thefirstlineofcode.sand.server.things.ThingAuthorization;

public class D_ThingAuthorization extends ThingAuthorization implements IIdProvider<String> {
	private String id;

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

}
