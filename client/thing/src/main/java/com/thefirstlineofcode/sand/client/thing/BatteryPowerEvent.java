package com.thefirstlineofcode.sand.client.thing;

public class BatteryPowerEvent implements IEvent<IThing, Integer> {
	private IThing source;
	private Integer batteryPower;
	
	public BatteryPowerEvent(IThing source, Integer batteryPower) {
		this.source = source;
		this.batteryPower = batteryPower;
	}
	
	@Override
	public IThing getSource() {
		return source;
	}
	
	@Override
	public Class<Integer> getEventType() {
		return Integer.class;
	}
	
	@Override
	public Integer getEvent() {
		return batteryPower;
	}

}
