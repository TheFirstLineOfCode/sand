package com.thefirstlineofcode.sand.demo.server;

import java.util.concurrent.ArrayBlockingQueue;

import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;

@AppComponent("not.authorized.edge.thing.registrations")
public class NotAuthorizedEdgeThingRegistrations {
	private ArrayBlockingQueue<String> notAuthorizedEdgeThingRegistrations;
	
	public NotAuthorizedEdgeThingRegistrations() {
		notAuthorizedEdgeThingRegistrations = new ArrayBlockingQueue<>(5, true);
	}
	
	public void put(String thingId) {
		try {			
			notAuthorizedEdgeThingRegistrations.put(thingId);
		} catch (Exception e) {
			throw new RuntimeException("Can't put thing into queue.", e);
		}
	}
	
	public boolean isEmpty() {
		return notAuthorizedEdgeThingRegistrations.isEmpty();
	}
	
	public String poll() {
		return notAuthorizedEdgeThingRegistrations.poll();
	}
}
