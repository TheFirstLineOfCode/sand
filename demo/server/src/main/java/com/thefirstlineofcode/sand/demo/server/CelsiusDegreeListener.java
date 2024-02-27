package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAllowed;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.sand.protocols.sensor.Report;
import com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter.CelsiusDegree;
import com.thefirstlineofcode.sand.server.sensor.IDataListener;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class CelsiusDegreeListener implements IDataListener<CelsiusDegree>, IServerConfigurationAware {
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IAclService aclService;
	
	@BeanDependency
	private IResourcesService resourceService;
	
	private String domain;
	
	private boolean deliverTemperatureToOwner;
	private String owner;
	
	public CelsiusDegreeListener() {
		deliverTemperatureToOwner = false;
	}
	
	@Override
	public void dataReceived(IProcessingContext context, JabberId reporter, CelsiusDegree celsiusDegree) {
		if (!deliverTemperatureToOwner)
			return;
		
		String thingId = thingManager.getEdgeThingManager().getThingIdByThingName(context.getJid().getNode());
		if (thingId == null)
			throw new ProtocolException(new NotAllowed("Reporter isn't a thing."));
		
		owner = getOwner(thingId);
		
		IResource[] resources = resourceService.getResources(new JabberId(owner, domain));
		if (resources == null || resources.length == 0)
			return;
		
		for (IResource resource : resources) {
			context.write(resource.getJid(), new Iq(Iq.Type.SET, new Report(celsiusDegree)));
		}
	}

	private String getOwner(String thingId) {
		if (owner != null)
			return owner;
		
		return aclService.getOwner(thingId);
	}
	
	public void enableDeliverTemperatureToOwner(boolean enabled) {
		deliverTemperatureToOwner = enabled;
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domain = serverConfiguration.getDomainName();
	}
}
