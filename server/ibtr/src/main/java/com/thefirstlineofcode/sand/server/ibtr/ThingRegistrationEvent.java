package com.thefirstlineofcode.sand.server.ibtr;

import java.util.Date;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;

public class ThingRegistrationEvent implements IEvent {
	private String thingId;
	private String thingName;
	private String authorizer;
	private Date registrationTime;
	
	public ThingRegistrationEvent(String thingId, String thingName, String authorizer, Date registrationTime) {
		this.thingId = thingId;
		this.thingName = thingName;
		this.authorizer = authorizer;
		this.registrationTime = registrationTime;
	}
	
	public String getThingId() {
		return thingId;
	}
	
	public String getThingName() {
		return thingName;
	}
	
	public String getAuthorizer() {
		return authorizer;
	}
	
	public Date getRegistrationTime() {
		return registrationTime;
	}
	
	@Override
	public Object clone() {
		return new ThingRegistrationEvent(thingId, thingName, authorizer, registrationTime);
	}
}
