package com.thefirstlineofcode.sand.demo.server;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideos;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakePhoto;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class RecordedVideosProcessor implements IXepProcessor<Iq, RecordedVideos> {
	
	@BeanDependency
	private IRecordedVideoManager recordedVideoManager;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@Override
	public void process(IProcessingContext context, Iq iq, RecordedVideos xep) {
		if (iq.getType() != Iq.Type.GET)
			throw new ProtocolException(new BadRequest("IQ type should be 'GET'."));
		
		if (xep.getRecorderThingId() == null)
			throw new ProtocolException(new BadRequest("Null recorder thing ID."));
		
		if (!thingManager.thingIdExists(xep.getRecorderThingId()))
			throw new ProtocolException(new ItemNotFound());
		
		String model = thingManager.getModel(xep.getRecorderThingId());
		if (!thingManager.getModelDescriptor(model).getSupportedActions().containsKey(TakePhoto.PROTOCOL))
			throw new ProtocolException(new ServiceUnavailable());
		
		List<RecordedVideo> recordedVideos = recordedVideoManager.findByRecorderThingId(xep.getRecorderThingId());
		
		Iq result = Iq.createResult(iq, new RecordedVideos(recordedVideos));
		context.write(context.getJid(), result);
	}

}
