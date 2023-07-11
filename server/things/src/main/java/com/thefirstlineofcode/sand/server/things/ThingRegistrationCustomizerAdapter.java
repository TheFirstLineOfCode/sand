package com.thefirstlineofcode.sand.server.things;

import java.util.Random;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;

public class ThingRegistrationCustomizerAdapter implements IThingRegistrationCustomizer {
	protected IThingManager thingManager;
	
	@Override
	public boolean isUnregisteredThing(String thingId, String registrationCode) {
		if (thingId == null || thingId.length() == 0)
			throw new ProtocolException(new BadRequest("Null thing ID."));
		
		if (thingManager.isRegistered(thingId))
			throw new ProtocolException(new Conflict());
		
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
	public boolean isAuthorizationRequired() {
		return true;
	}
	
	@Override
	public boolean isConfirmationRequired() {
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
