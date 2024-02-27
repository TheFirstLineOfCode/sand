package com.thefirstlineofcode.sand.server.console;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmed;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmedEvent;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@AppComponent("node.confirmed.listener")
public class NodeConfirmedListener implements IEventListener<NodeConfirmedEvent>, IServerConfigurationAware {
	private String domainName;
	private List<ConfirmedConcentration> confirmedConcentrations;
	
	@BeanDependency
	private IThingManager thingManager;
	
	public NodeConfirmedListener() {
		confirmedConcentrations = new ArrayList<>();
	}

	@Override
	public void process(IEventContext context, NodeConfirmedEvent event) {
		NodeConfirmed nodeConfirmed = event.getNodeConfirmed();
		
		String concentratorThingName = nodeConfirmed.getNodeAdded().getConcentratorThingName();
		String concentratorThingId = thingManager.getEdgeThingManager().getThingIdByThingName(concentratorThingName);
		String nodeThingId = nodeConfirmed.getNodeAdded().getNodeThingId();
		
		ConfirmedConcentration found = null;
		for (int i = 0; i < confirmedConcentrations.size(); i++) {
			ConfirmedConcentration confirmedConcentration = confirmedConcentrations.get(i);
			if (confirmedConcentration.concentratorThingId.equals(concentratorThingId) &&
				confirmedConcentration.nodeThingId.equals(nodeThingId)) {
				found = confirmedConcentration;
				break;
			}
		}
		
		if (found == null)
			return;
		
		confirmedConcentrations.remove(found);
		
		Iq resultToConcentrator = new Iq(Iq.Type.RESULT, nodeConfirmed.getRequestId());
		JabberId jidConcentrator = JabberId.parse(String.format("%s@%s/%d", concentratorThingName,
				domainName, IConcentrator.LAN_ID_CONCENTRATOR));
		resultToConcentrator.setTo(jidConcentrator);
		resultToConcentrator.setObject(nodeConfirmed.getNodeAdded());
		context.write(jidConcentrator, resultToConcentrator);
	}
	
	public void addConfirmedConcentration(ConfirmedConcentration confirmedConcentration) {
		if (confirmedConcentrations.contains(confirmedConcentration))
			return;
		
		confirmedConcentrations.add(confirmedConcentration);
	}
	
	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}
}
