package com.thefirstlineofcode.sand.demo.protocols;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace = "http://thefirstlineofcode.com/sand-demo", localName = "recorded-videos")
public class RecordedVideos {
	public static final Protocol PROTOCOL = new Protocol("http://thefirstlineofcode.com/sand-demo", "recorded-videos");
	
	private String recorderThingId;
	@Array(elementName="recorded-video", value=RecordedVideo.class)
	private List<RecordedVideo> recordedVideos;
	
	public RecordedVideos() {}
	
	public RecordedVideos(String recorderThingId) {
		this.recorderThingId = recorderThingId;
	}
	
	public RecordedVideos(List<RecordedVideo> recordedVideos) {
		this.recordedVideos = recordedVideos;
	}
	
	public String getRecorderThingId() {
		return recorderThingId;
	}
	
	public void setRecorderThingId(String recorderThingId) {
		this.recorderThingId = recorderThingId;
	}
	
	public List<RecordedVideo> getRecordedVideos() {
		return recordedVideos;
	}
	
	public void setRecordedVideos(List<RecordedVideo> recordedVideos) {
		this.recordedVideos = recordedVideos;
	}
	
	
	
}
