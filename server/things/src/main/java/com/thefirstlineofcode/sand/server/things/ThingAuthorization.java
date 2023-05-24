package com.thefirstlineofcode.sand.server.things;

import java.util.Date;

public class ThingAuthorization {
	private String thingId;
	private String authorizer;
	private Date authorizedTime;
	private Date expiredTime;
	private boolean canceled;
	
	public String getThingId() {
		return thingId;
	}
	
	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public String getAuthorizer() {
		return authorizer;
	}
	
	public void setAuthorizer(String authorizer) {
		this.authorizer = authorizer;
	}
	
	public Date getAuthorizedTime() {
		return authorizedTime;
	}
	
	public void setAuthorizedTime(Date authorizeTime) {
		this.authorizedTime = authorizeTime;
	}
	
	public Date getExpiredTime() {
		return expiredTime;
	}
	
	public void setExpiredTime(Date expiredTime) {
		this.expiredTime = expiredTime;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
	
}
