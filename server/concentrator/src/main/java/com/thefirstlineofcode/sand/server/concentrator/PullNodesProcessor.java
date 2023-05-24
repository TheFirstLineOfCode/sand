package com.thefirstlineofcode.sand.server.concentrator;

import java.util.Arrays;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlinelinecode.sand.protocols.concentrator.PullNodes;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAllowed;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class PullNodesProcessor implements IXepProcessor<Iq, PullNodes> {
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;

	@Override
	public void process(IProcessingContext context, Iq iq, PullNodes xep) {
		if (iq.getType() != Iq.Type.GET)
			throw new ProtocolException(new BadRequest("IQ type should be 'GET'."));
		
		String thingName = context.getJid().getNode();
		String thingId = thingManager.getThingIdByThingName(thingName);
		String model = thingManager.getModel(thingId);
		
		if (!thingManager.isConcentrator(model))
			throw new ProtocolException(new NotAllowed());
		
		IConcentrator concentrator = concentratorFactory.getConcentrator(thingId);
		Node[] nodes = concentrator.getNodes();
		
		Iq result = Iq.createResult(iq, new PullNodes(Arrays.asList(nodes)));
		context.write(result);
	}

}
