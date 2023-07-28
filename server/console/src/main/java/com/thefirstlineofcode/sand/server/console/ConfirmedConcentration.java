package com.thefirstlineofcode.sand.server.console;

public class ConfirmedConcentration {
	public String concentratorThingId;
	public String nodeThingId;
	
	public ConfirmedConcentration(String concentratorThingId, String nodeThingId) {
		this.concentratorThingId = concentratorThingId;
		this.nodeThingId = nodeThingId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		
		if (obj instanceof ConfirmedConcentration) {
			ConfirmedConcentration other = (ConfirmedConcentration)obj;
			
			return concentratorThingId.equals(other.concentratorThingId) &&
					nodeThingId.equals(other.nodeThingId);
		}
		
		return false;
	}
}
