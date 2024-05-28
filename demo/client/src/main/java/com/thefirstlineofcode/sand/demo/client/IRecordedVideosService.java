package com.thefirstlineofcode.sand.demo.client;

import java.util.List;

import com.thefirstlineofcode.chalk.core.ErrorException;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;

public interface IRecordedVideosService {
	List<RecordedVideo> getRecordedVideos(String recorderThingId) throws ErrorException;
	void removeRecordedVideo(String videoName) throws ErrorException;
}
