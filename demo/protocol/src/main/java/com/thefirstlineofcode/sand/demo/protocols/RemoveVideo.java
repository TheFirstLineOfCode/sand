package com.thefirstlineofcode.sand.demo.protocols;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "remove-video")
public class RemoveVideo {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "remove-video");
	
	private String videoName;
	
	public RemoveVideo() {}
	
	public RemoveVideo(String videoName) {
		this.videoName = videoName;
	}

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}
}
