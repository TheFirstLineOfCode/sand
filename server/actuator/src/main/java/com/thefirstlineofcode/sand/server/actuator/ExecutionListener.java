package com.thefirstlineofcode.sand.server.actuator;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IIqResultProcessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class ExecutionListener implements IEventListener<ExecutionEvent>, IIqResultProcessor, IServerConfigurationAware {
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@BeanDependency
	private IThingManager thingManager;
	
	private String domain;
	
	private Map<String, IExecutionCallback> callbacks;
	
	public ExecutionListener() {
		callbacks = new HashMap<>();
	}
	
	@Override
	public void process(IEventContext context, ExecutionEvent event) {
		String thingId = event.getThingId();
		String actuatorThingId = thingId;
		
		boolean isConcentrator = concentratorFactory.isConcentrator(thingId);
		if (isConcentrator && event.getLanId() != null) {
			IConcentrator concentrator = concentratorFactory.getConcentrator(thingId);
			if (!concentrator.containsLanId(event.getLanId())) {
				throw new IllegalArgumentException(String.format("Concentrator '%s' doesn't contain a node which's LAN ID is '%s'.",
						thingId, event.getLanId()));
			}
			
			Node node = concentrator.getNodeByLanId(event.getLanId());
			actuatorThingId = node.getThingId();
		}
		
		String model = thingManager.getModel(actuatorThingId);
		if (!thingManager.isActionSupported(model, event.getExecution().getAction().getClass())) {
			throw new IllegalArgumentException(String.format("Unsupported action type: '%s'.", event.getExecution().getAction().getClass().getName()));
		}
		
		String thingName = thingManager.getThingNameByThingId(thingId);
		
		Iq iq = new Iq(Iq.Type.SET, event.getExecution());
		
		if (isConcentrator && event.getLanId() != null) {			
			iq.setTo(new JabberId(thingName, domain, String.valueOf(event.getLanId())));
		} else {
			iq.setTo(new JabberId(thingName, domain, ThingIdentity.DEFAULT_RESOURCE_NAME));
		}
		
		synchronized (this) {
			if (event.getExecutionCallback() != null)
				callbacks.put(iq.getId(), event.getExecutionCallback());			
		}
		
		context.write(getTarget(event, thingName, isConcentrator), iq);
	}
	
	private JabberId getTarget(ExecutionEvent event, String thingName, boolean isConcentrator) {
		if (!isConcentrator && event.getLanId() != null) {
			throw new IllegalArgumentException("Thing which's ID is %s isn't a concentrator.");
		}
		
		JabberId to = new JabberId();
		to.setNode(thingName);
		to.setDomain(domain);
		
		if (!isConcentrator) {
			to.setResource(ThingIdentity.DEFAULT_RESOURCE_NAME);
		} else {			
			to.setResource(String.valueOf(IConcentrator.LAN_ID_CONCENTRATOR));
		}
		
		return to;
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domain = serverConfiguration.getDomainName();
	}

	@Override
	public boolean processResult(IProcessingContext context, Iq result) {
		IExecutionCallback callback = getCallback(result);
		
		boolean processed = false;
		if (callback != null)
			processed = callback.processResult(context, result);
		
		if (processed)
			return true;
		
		context.write(result);
		return true;
	}

	private synchronized IExecutionCallback getCallback(Stanza stanza) {
			String id = stanza.getId();
			
			IExecutionCallback callback = callbacks.get(id);
			if (callback != null)
				callbacks.remove(id);
			
			return callback;
	}

	@Override
	public boolean processError(IProcessingContext context, StanzaError error) {
		IExecutionCallback callback = getCallback(error);
		
		boolean processed = false;
		if (callback != null)
			processed = callback.processError(context, error);
		
		if (processed)
			return true;
		
		context.write(error);;
		return true;
	}

}
