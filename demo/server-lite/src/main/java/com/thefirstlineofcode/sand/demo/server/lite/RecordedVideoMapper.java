package com.thefirstlineofcode.sand.demo.server.lite;

import java.util.List;

import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;

public interface RecordedVideoMapper {
	void insert(RecordedVideo recordedVideo);
	void delete(String videoName);
	int selectCountByVideoName(String videoName);
	List<RecordedVideo> selectByRecorder(String recorderThingId);
}
