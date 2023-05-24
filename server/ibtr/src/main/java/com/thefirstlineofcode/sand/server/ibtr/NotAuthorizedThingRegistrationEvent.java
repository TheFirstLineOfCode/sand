package com.thefirstlineofcode.sand.server.ibtr;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;

public class NotAuthorizedThingRegistrationEvent implements IEvent {
	private String thingId;
	
	public NotAuthorizedThingRegistrationEvent(String thingId) {
		this.thingId = thingId;
	}
	
	public String getThingId() {
		return thingId;
	}
	
	@Override
	public Object clone() {
		return new NotAuthorizedThingRegistrationEvent(thingId);
	}
}
