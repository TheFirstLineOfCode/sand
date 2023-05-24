package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Date;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.Int2Enum;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "video-recorded")
public class VideoRecorded {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "video-recorded");
	
	public enum RecordingReason {
		USER_ACTION,
		IOT_EVENT
	}
	
	private String videoName;
	private String videoUrl;
	private Date recordingTime;
	@Int2Enum(RecordingReason.class)
	private RecordingReason recordingReason;
	
	public VideoRecorded() {}
	
	public VideoRecorded(String videoName, String videoUrl, Date recordingTime, RecordingReason reason) {
		this.videoName = videoName;
		this.videoUrl = videoUrl;
		this.recordingTime = recordingTime;
		this.recordingReason = reason;
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
	
	public Date getRecordingTime() {
		return recordingTime;
	}
	
	public void setRecordingTime(Date recordingTime) {
		this.recordingTime = recordingTime;
	}
	
	public RecordingReason getRecordingReason() {
		return recordingReason;
	}
	
	public void setRecordingReason(RecordingReason reason) {
		this.recordingReason = reason;
	}
}
