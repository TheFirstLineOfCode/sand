package com.thefirstlineofcode.sand.server.concentrator;

import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class NodeAdditionListener implements IEventListener<NodeAdditionEvent>, IServerConfigurationAware{
	@BeanDependency
	private IThingManager thingManager;
	
	private String domainName;
	
	@Override
	public void process(IEventContext context, NodeAdditionEvent event) {
		String concentratorThingName = thingManager.getThingNameByThingId(event.getConcentratorThingName());
		if (concentratorThingName == null)
			throw new RuntimeException("Concentrator not existed?");
		
		Iq result = new Iq(Iq.Type.RESULT, event.getRequestId());		
		result.setTo(getConcentratorJid(event));
		result.setObject(new NodeAdded(concentratorThingName, event.getNodeThingId(), event.getLanId(),
				event.getModel(), event.getAdditionTime()));
		
		context.write(result);
	}

	private JabberId getConcentratorJid(NodeAdditionEvent event) {
		JabberId jid = new JabberId();
		jid.setNode(event.getConcentratorThingName());
		jid.setDomain(domainName);
		jid.setResource(String.valueOf(IConcentrator.LAN_ID_CONCENTRATOR));
		
		return jid;
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}
}
