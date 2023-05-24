package com.thefirstlineofcode.sand.demo.server.lite;

import com.thefirstlineofcode.granite.framework.core.adf.data.IIdProvider;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;

public class D_RecordedVideo extends RecordedVideo implements IIdProvider<String> {
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
