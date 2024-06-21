package com.thefirstlineofcode.sand.protocols.thing;

import java.util.Collections;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public abstract class AbstractThingDescriptor implements IThingDescriptor {
	protected boolean actuator;
	protected boolean concentrator;
	protected boolean sensor;
	
	protected Map<Protocol, Class<?>> supportedActions;
	protected Map<Protocol, Class<?>> supportedActionResults;
	protected Map<Protocol, Class<?>> supportedEvents;
	protected Map<Protocol, Class<?>> followedEvents;
	protected Map<Protocol, Class<?>> supportedData;
	
	public AbstractThingDescriptor(Map<Protocol, Class<?>> supportedEvents) {
		this(false, supportedEvents);
	}
	
	public AbstractThingDescriptor(boolean concentrator, Map<Protocol, Class<?>> supportedEvents) {
		this(concentrator, supportedEvents, null, null);
	}
	
	public AbstractThingDescriptor(boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData) {
		this(concentrator, supportedEvents, supportedData, null);
	}
	
	public AbstractThingDescriptor(boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData, Map<Protocol, Class<?>> suppportedActions) {
		this(concentrator, supportedEvents, supportedData, suppportedActions, null);
	}
	
	public AbstractThingDescriptor(boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData, Map<Protocol, Class<?>> suppportedActions,
				Map<Protocol, Class<?>> suppportedActionResults) {
		this(concentrator, supportedEvents, supportedData, suppportedActions, suppportedActionResults, null);
	}
	
	public AbstractThingDescriptor(boolean concentrator, Map<Protocol, Class<?>> supportedEvents,
			Map<Protocol, Class<?>> supportedData, Map<Protocol, Class<?>> suppportedActions,
			Map<Protocol, Class<?>> suppportedActionResults, Map<Protocol, Class<?>> followedEvents) {
		this.concentrator = concentrator;
		this.supportedEvents = supportedEvents;
		this.supportedData = supportedData;
		this.supportedActions = suppportedActions;
		this.supportedActionResults = suppportedActionResults;
		this.followedEvents = followedEvents;
		
		if (supportedData != null) {
			sensor = true;
		}
		
		if (suppportedActions != null) {
			actuator = true;
		}
	}
	
	public boolean isActuator() {
		return actuator;
	}
	
	public boolean isConcentrator() {
		return concentrator;
	}
	
	public boolean isSensor() {
		return sensor;
	}
	
	public Map<Protocol, Class<?>> getSupportedEvents() {
		return supportedEvents == null ? createEmptyMap() : Collections.unmodifiableMap(supportedEvents);
	}
	
	@Override
	public Map<Protocol, Class<?>> getFollowedEvents() {
		return followedEvents == null ? createEmptyMap() : Collections.unmodifiableMap(followedEvents);
	}
	
	public Map<Protocol, Class<?>> getSupportedData() {
		return supportedData == null ? createEmptyMap() : Collections.unmodifiableMap(supportedData);
	}
	
	public Map<Protocol, Class<?>> getSupportedActions() {
		return supportedActions == null ? createEmptyMap() : Collections.unmodifiableMap(supportedActions);
	}
	
	public Map<Protocol, Class<?>> getSupportedActionResults() {
		return supportedActionResults == null ? createEmptyMap() : Collections.unmodifiableMap(supportedActionResults);
	}

	private Map<Protocol, Class<?>> createEmptyMap() {
		return Collections.emptyMap();
	}
}
