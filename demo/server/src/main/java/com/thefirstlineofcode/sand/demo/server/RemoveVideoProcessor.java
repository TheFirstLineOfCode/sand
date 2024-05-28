package com.thefirstlineofcode.sand.demo.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.InternalServerError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.demo.protocols.RemoveVideo;

public class RemoveVideoProcessor implements IXepProcessor<Iq, RemoveVideo>  {
	@BeanDependency
	private IRecordedVideoManager videoManager;
	
	private Path recordedVideosRootLocation;
	
	public RemoveVideoProcessor() {
		recordedVideosRootLocation = Paths.get(System.getProperty("user.home"), "recorded-videos-dir");
	}

	@Override
	public void process(IProcessingContext context, Iq iq, RemoveVideo xep) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("Attribute 'type' should be set to 'set'."));
		
		if (xep.getVideoName() == null)
			throw new ProtocolException(new BadRequest("Null video name."));
		
		if (!videoManager.exists(xep.getVideoName()))
			throw new ProtocolException(new ItemNotFound());
		
		try {
			Files.delete(recordedVideosRootLocation.resolve(xep.getVideoName()));
		} catch (IOException e) {
			throw new ProtocolException(new InternalServerError(String.format("Failed to remove video: %s.", xep.getVideoName())));
		}
		
		videoManager.remove(xep.getVideoName());
		
		context.write(Iq.createResult(iq));
	}

}
