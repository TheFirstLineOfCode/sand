package com.thefirstlineofcode.sand.server.friends;

import java.util.Date;

public class FollowApproval {
	private Follow follow;
	private String approver;
	private Date approvalTime;
	
	public Follow getFollow() {
		return follow;
	}
	
	public void setFollow(Follow follow) {
		this.follow = follow;
	}
	
	public String getApprover() {
		return approver;
	}
	
	public void setApprover(String approver) {
		this.approver = approver;
	}
	
	public Date getApprovalTime() {
		return approvalTime;
	}
	
	public void setApprovalTime(Date approvalTime) {
		this.approvalTime = approvalTime;
	}
		
}
