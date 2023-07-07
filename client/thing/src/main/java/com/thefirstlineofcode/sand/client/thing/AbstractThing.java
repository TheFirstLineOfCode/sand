package com.thefirstlineofcode.sand.client.thing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractThing implements IThing {
	protected static final String ATTRIBUTE_NAME_THING_ID = "thing_id";
	
	protected String thingId;
	protected String model;
	protected boolean powered;
	protected int batteryPower;
	
	protected Map<String, String> attributes;
	protected boolean attributesChanged;
	
	protected List<IBatteryPowerListener> batteryPowerlisteners;
	
	public AbstractThing() {}
	
	public AbstractThing(String model) {
		this.model = model;
		
		powered = false;
		batteryPower = 0;
		
		batteryPowerlisteners =  new ArrayList<>();
		
		attributes = loadThingAttributes();		
		if (attributes == null)
			attributes = new HashMap<>();
		
		attributesChanged = false;
		
		thingId = getThingId(attributes);
		
		if (thingId == null) {
			thingId = loadThingId();
			
			if (thingId == null)
				throw new RuntimeException("Failed to generate thing ID. Null thing ID.");
			
			attributes.put(ATTRIBUTE_NAME_THING_ID, thingId);
			attributesChanged = true;
		}
	}
	
	protected String getThingId(Map<String, String> attributes) {
		String thingId = attributes.get(ATTRIBUTE_NAME_THING_ID);
		
		return thingId == null ? null : thingId.trim();
	}
	
	protected abstract Map<String, String> loadThingAttributes();
	protected abstract String loadThingId();
	protected abstract void saveAttributes(Map<String, String> attributes);
	protected abstract String loadRegistrationCode();
	
	@Override
	public String getThingId() {
		return thingId;
	}
	
	@Override
	public String getThingModel() {
		return model;
	}
	
	@Override
	public void addBatteryPowerListener(IBatteryPowerListener listener) {
		if (!batteryPowerlisteners.contains(listener))
			batteryPowerlisteners.add(listener);
	}

	@Override
	public boolean removeBatteryPowerListener(IBatteryPowerListener listener) {
		return batteryPowerlisteners.remove(listener);
	}
	
	@Override
	public boolean isPowered() {
		return powered && getBatteryPower() != 0;
	}
	
	@Override
	public int getBatteryPower() {
		return batteryPower;
	}

}
