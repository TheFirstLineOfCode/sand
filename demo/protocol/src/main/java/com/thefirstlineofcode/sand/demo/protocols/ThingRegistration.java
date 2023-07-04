package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Date;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "thing-registration")
public class ThingRegistration {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "thing-registration");
	
	private String thingId;
	private String thingName;
	private String authorizer;
	private Date registrationTime;
	
	public ThingRegistration() {}
	
	public ThingRegistration(String thingId, String thingName, String authorizer, Date registrationTime) {
		this.thingId = thingId;
		this.thingName = thingName;
		this.authorizer = authorizer;
		this.registrationTime = registrationTime;
	}
	
	public String getThingId() {
		return thingId;
	}
	
	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public String getThingName() {
		return thingName;
	}
	
	public void setThingName(String thingName) {
		this.thingName = thingName;
	}
	
	public String getAuthorizer() {
		return authorizer;
	}
	
	public void setAuthorizer(String authorizer) {
		this.authorizer = authorizer;
	}
	
	public Date getRegistrationTime() {
		return registrationTime;
	}
	
	public void setRegistrationTime(Date registrationTime) {
		this.registrationTime = registrationTime;
	}
}
