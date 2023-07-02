package com.thefirstlineofcode.sand.server.things;

import java.util.Random;

import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;

public abstract class AbstractThingRegistrationCustomizer implements IThingRegistrationCustomizer {
	protected IThingManager thingManager;
	
	@Override
	public boolean isUnregisteredThing(String thingId, String registrationKey) {
		if (thingId == null || thingId.length() == 0)
			return false;
		
		IThingModelDescriptor[] modelDescriptors = thingManager.getModelDescriptors();
		for  (IThingModelDescriptor modelDescriptor : modelDescriptors) {
			if (thingId.length() > modelDescriptor.getModelName().length() &&
					thingId.startsWith(modelDescriptor.getModelName() + "-") &&
					thingId.substring(modelDescriptor.getModelName().length(), thingId.length()).length() == 9)
				return true;
		}
		
		return false;
	}
	
	@Override
	public String guessModel(String thingId) {
		IThingModelDescriptor[] modelDescriptors = thingManager.getModelDescriptors();
		for (IThingModelDescriptor modelDescriptor : modelDescriptors) {
			if (thingId.startsWith(modelDescriptor.getModelName()))
				return modelDescriptor.getModelName();
		}
		
		return null;
	}

	@Override
	public boolean isAuthenticationRequired() {
		return true;
	}

	@Override
	public void setThingManager(IThingManager thingManager) {
		this.thingManager = thingManager;
	}

	@Override
	public byte[] createSecurityKey() {
		byte[] securityKey = new byte[16];
		Random random = new Random(System.currentTimeMillis());
		random.nextBytes(securityKey);
		
		return securityKey;
	}

}
