package com.thefirstlineofcode.sand.demo.server.lite;

import java.util.List;

import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;

public interface RecordedVideoMapper {
	void insert(RecordedVideo recordedVideo);
	List<RecordedVideo> selectByRecorder(String recorderThingId);
}
