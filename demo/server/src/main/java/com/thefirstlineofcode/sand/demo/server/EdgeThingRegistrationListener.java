package com.thefirstlineofcode.sand.demo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactory;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;
import com.thefirstlineofcode.sand.demo.protocols.EdgeThingRegistration;
import com.thefirstlineofcode.sand.server.ibtr.EdgeThingRegistrationEvent;

public class EdgeThingRegistrationListener implements IEventListener<EdgeThingRegistrationEvent>,
		IServerConfigurationAware, IDataObjectFactoryAware {
	private static final String USER_NAME_SAND_DEMO = "sand-demo";

	private static final Logger logger = LoggerFactory.getLogger(EdgeThingRegistrationListener.class);
	
	@BeanDependency
	private IAclService aclService;
	
	@BeanDependency
	private IResourcesService resourceService;
	
	private IDataObjectFactory dataObjectFactory;
	private String domainName;
	
	@Override
	public void process(IEventContext context, EdgeThingRegistrationEvent event) {
		String authorizer = event.getAuthorizer();
		if (authorizer == null)
			authorizer = USER_NAME_SAND_DEMO;
		
		createAce(event.getThingId(), authorizer);
		
		IResource[] resources = resourceService.getResources(JabberId.parse(String.format("%s@%s", event.getAuthorizer(), domainName)));	
		if (resources == null || resources.length == 0 && logger.isWarnEnabled()) {
			logger.warn("Can't find any resource for authorizer '{}'. Ignore to pass edge thing registration stanza to the owner.", authorizer);
			return;
		}
			
		for (IResource resource : resources) {			
			Iq iq = new Iq(Iq.Type.SET);
			iq.setTo(resource.getJid());	
			iq.setObject(new EdgeThingRegistration(event.getThingId(), event.getThingName(),
					event.getAuthorizer(), event.getRegistrationTime()));
			
			context.write(iq);
		}
	}
	
	private void createAce(String thingId, String owner) {
		AccessControlEntry ace = dataObjectFactory.create(AccessControlEntry.class);
		ace.setUser(owner);
		ace.setThingId(thingId);
		ace.setRole(Role.OWNER);
		
		aclService.add(ace);
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		this.domainName = serverConfiguration.getDomainName();
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}
}
