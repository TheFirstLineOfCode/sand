package com.thefirstlineofcode.sand.demo.server;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;

public interface IRecordedVideoManager {
	void add(JabberId recorder, VideoRecorded videoRecorded);
	void remove(String videoName);
	boolean exists(String videoName);
	List<RecordedVideo> findByRecorderThingId(String recorderThingId);
	
}
