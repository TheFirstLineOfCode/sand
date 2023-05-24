package com.thefirstlineofcode.sand.server.lite.things;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

public class D_ThingIdentity extends ThingIdentity implements IIdProvider<String> {
	private static final long serialVersionUID = 6554323836858199250L;
	
	private String id;
	private String thingId;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
}
