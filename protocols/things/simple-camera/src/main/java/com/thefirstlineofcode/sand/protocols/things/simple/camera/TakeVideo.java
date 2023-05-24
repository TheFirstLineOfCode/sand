package com.thefirstlineofcode.sand.protocols.things.simple.camera;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:things:simple-camera", localName="take-video")
public class TakeVideo {
	public static Protocol PROTOCOL = new Protocol("urn:leps:things:simple-camera", "take-video");
	
	private Integer durationTime;
	private String videoName;
	private String videoUrl;
	
	public TakeVideo() {}
	
	public TakeVideo(Integer durationTime) {
		this.durationTime = durationTime;
	}
	
	public TakeVideo(String videoName, String videoUrl) {
		this.videoName = videoName;
		this.videoUrl = videoUrl;
	}

	public Integer getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(Integer durationTime) {
		this.durationTime = durationTime;
	}

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
}
