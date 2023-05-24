package com.thefirstlineofcode.sand.demo.protocols;

import java.util.Date;

import com.thefirstlineofcode.basalt.oxm.coc.conversion.annotations.Int2Enum;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded.RecordingReason;

public class RecordedVideo {
	private String recorderThingId;
	private String videoName;
	private String videoUrl;
	private Date recordingTime;
	@Int2Enum(RecordingReason.class)
	private RecordingReason recordingReason;
	private Date storedTime;
	
	public RecordedVideo() {}
	
	public RecordedVideo(String recordThingId, String videoName, String videoUrl, Date recordingTime,
			RecordingReason recordingReason, Date storedTime) {
		this.recorderThingId = recordThingId;
		this.videoName = videoName;
		this.videoUrl = videoUrl;
		this.recordingTime = recordingTime;
		this.recordingReason = recordingReason;
		this.storedTime = storedTime;
	}
	
	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}

	public String getRecorderThingId() {
		return recorderThingId;
	}

	public void setRecorderThingId(String recorderThingId) {
		this.recorderThingId = recorderThingId;
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

	public Date getStoredTime() {
		return storedTime;
	}

	public void setStoredTime(Date storedTime) {
		this.storedTime = storedTime;
	}
}
