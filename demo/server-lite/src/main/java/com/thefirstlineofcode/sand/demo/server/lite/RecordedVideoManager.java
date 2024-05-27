package com.thefirstlineofcode.sand.demo.server.lite;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactory;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;
import com.thefirstlineofcode.sand.demo.server.IRecordedVideoManager;
import com.thefirstlineofcode.sand.server.location.ILocationService;

@Component
@Transactional
public class RecordedVideoManager implements IRecordedVideoManager, IDataObjectFactoryAware {
	@Autowired
	private SqlSession sqlSession;
	
	@Autowired
	private ILocationService locationService;
	
	private IDataObjectFactory dataObjectFactory;
	
	@Override
	public void add(JabberId recorder, VideoRecorded videoRecorded) {
		if (videoRecorded.getVideoName() == null ||
				videoRecorded.getVideoUrl() == null ||
				videoRecorded.getRecordingTime() == null ||
				videoRecorded.getRecordingReason() == null)
			throw new ProtocolException(new BadRequest("Some required attributes of recorded video are null."));
		
		RecordedVideo recordedVideo = dataObjectFactory.create(RecordedVideo.class);
		
		String recorderThingId = locationService.getThingIdByJid(recorder);
		if (recorderThingId == null)
			throw new ProtocolException(new ItemNotFound());
		
		recordedVideo.setRecorderThingId(recorderThingId);
		recordedVideo.setVideoName(videoRecorded.getVideoName());
		recordedVideo.setVideoUrl(videoRecorded.getVideoUrl());
		recordedVideo.setRecordingTime(videoRecorded.getRecordingTime());
		recordedVideo.setRecordingReason(videoRecorded.getRecordingReason());
		
		getRecordedVideoMapper().insert(recordedVideo);
	}

	private RecordedVideoMapper getRecordedVideoMapper() {
		return sqlSession.getMapper(RecordedVideoMapper.class);
	}

	@Override
	public List<RecordedVideo> findByRecorderThingId(String recorderThingId) {
		return getRecordedVideoMapper().selectByRecorder(recorderThingId);
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}

	@Override
	public void remove(String videoName) {
		getRecordedVideoMapper().delete(videoName);
	}

	@Override
	public boolean exists(String videoName) {
		return getRecordedVideoMapper().selectCountByVideoName(videoName) != 0;
	}

}
