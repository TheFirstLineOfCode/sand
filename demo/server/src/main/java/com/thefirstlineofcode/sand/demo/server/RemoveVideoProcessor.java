package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.demo.protocols.RemoveVideo;

public class RemoveVideoProcessor implements IXepProcessor<Iq, RemoveVideo>  {
	@BeanDependency
	private IRecordedVideoManager videoManager;

	@Override
	public void process(IProcessingContext context, Iq iq, RemoveVideo xep) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("Attribute 'type' should be set to 'set'."));
		
		if (xep.getVideoName() == null)
			throw new ProtocolException(new BadRequest("Null video name."));
		
		if (!videoManager.exists(xep.getVideoName()))
			throw new ProtocolException(new ItemNotFound());
		
		videoManager.remove(xep.getVideoName());
		
		context.write(Iq.createResult(iq));
	}

}
