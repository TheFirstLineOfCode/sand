package com.thefirstlineofcode.sand.server.concentrator;

import com.thefirstlinelinecode.sand.protocols.concentrator.RemoveNode;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class RemoveNodeProcessor implements IXepProcessor<Iq, RemoveNode> {
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@BeanDependency
	private IAccountManager accountManager;
	
	@Override
	public void process(IProcessingContext context, Iq iq, RemoveNode removeNode) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		JabberId target = iq.getTo();
		
		if (target == null)
			throw new ProtocolException(new BadRequest("Null target."));
		
		if (target.getResource() != null && !String.valueOf(IConcentrator.LAN_ID_CONCENTRATOR).
				equals(target.getResource()))
			throw new ProtocolException(new BadRequest("Not a concentrator."));
		
		String concentratorThingName = target.getNode();
		if (!thingManager.thingNameExists(concentratorThingName))
			throw new ProtocolException(new ItemNotFound());
		
		String concentratorThingId = thingManager.getThingIdByThingName(concentratorThingName);
		if (!concentratorFactory.isConcentrator(concentratorThingId))
			throw new ProtocolException(new BadRequest("Not a concentrator."));
		
		IConcentrator concentrator = concentratorFactory.getConcentrator(concentratorThingId);
		if (!concentrator.containsLanId(removeNode.getLanId()))
			throw new ProtocolException(new ItemNotFound("No such node."));
		
		concentrator.removeNode(removeNode.getLanId());
		context.write(context.getJid(), Iq.createResult(iq));
		
		context.write(iq.getTo(), iq);		
	}
}
