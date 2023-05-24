package com.thefirstlineofcode.sand.demo.client;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.ErrorException;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ISyncTask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideos;

public class RecordedVideosService implements IRecordedVideosService {
	private IChatServices chatServices;
	
	@Override
	public List<RecordedVideo> getRecordedVideos(final String recorderThingId) throws ErrorException {
		return  chatServices.getTaskService().execute(new ISyncTask<Iq, List<RecordedVideo>>() {
			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				stream.send(new Iq(Iq.Type.GET, new RecordedVideos(recorderThingId)));
			}
			
			@Override
			public List<RecordedVideo> processResult(Iq iq) {
				RecordedVideos recordedVideos = iq.getObject();
				return recordedVideos.getRecordedVideos();
			}
		});
	}
}
