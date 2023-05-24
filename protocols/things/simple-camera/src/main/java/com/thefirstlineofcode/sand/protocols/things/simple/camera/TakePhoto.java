package com.thefirstlineofcode.sand.protocols.things.simple.camera;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:things:simple-camera", localName="take-photo")
public class TakePhoto {
	public static Protocol PROTOCOL = new Protocol("urn:leps:things:simple-camera", "take-photo");
	
	private Integer prepareTime;
	private String photoFileName;
	private String photoUrl;
	
	public TakePhoto() {}
	
	public TakePhoto(int prepareTime) {
		this.prepareTime = prepareTime;
	}
	
	public TakePhoto(String photoFileName, String photoUrl) {
		this.photoFileName = photoFileName;
		this.photoUrl = photoUrl;
	}
	
	public Integer getPrepareTime() {
		return prepareTime;
	}

	public void setPrepareTime(Integer prepareTime) {
		this.prepareTime = prepareTime;
	}

	public String getPhotoFileName() {
		return photoFileName;
	}

	public void setPhotoFileName(String photoFileName) {
		this.photoFileName = photoFileName;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}
	
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
}
