package com.thefirstlineofcode.sand.protocols.ibtr.oxm;

import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.ibtr.ThingRegister;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.protocols.thing.UnregisteredThing;

public class ThingRegisterTranslatorFactory implements ITranslatorFactory<ThingRegister> {
	private ITranslator<ThingRegister> translator = new ThingRegisterTranslator();

	@Override
	public Class<ThingRegister> getType() {
		return ThingRegister.class;
	}

	@Override
	public ITranslator<ThingRegister> create() {
		return translator;
	}
	
	private class ThingRegisterTranslator implements ITranslator<ThingRegister> {

		@Override
		public Protocol getProtocol() {
			return ThingRegister.PROTOCOL;
		}

		@Override
		public String translate(ThingRegister iqRegister, IProtocolWriter writer,
				ITranslatingFactory translatingFactory) {
			writer.writeProtocolBegin(ThingRegister.PROTOCOL);
			
			Object register = iqRegister.getRegister();
			
			if (register == null) {
				throw new RuntimeException("Null register object.");
			}
			
			if (register instanceof UnregisteredThing) {
				UnregisteredThing unregisteredThing = (UnregisteredThing)register;
				writer.writeElementBegin("unregistered-thing").
				writeTextOnly("thing-id", unregisteredThing.getThingId()).
				writeTextOnly("registration-key", unregisteredThing.getRegistrationCode()).
				writeElementEnd();
			} else if (register instanceof RegisteredThing) {
				RegisteredThing registeredThing = (RegisteredThing)register;
				writer.writeElementBegin("registered-thing").
					writeTextOnly("thing-name", registeredThing.getThingName()).
					writeTextOnly("credentials", registeredThing.getCredentials()).
					writeTextOnly("security-key", BinaryUtils.encodeToBase64(registeredThing.getSecurityKey())).					
				writeElementEnd();
			} else {
				throw new RuntimeException("Unknown register object.");
			}
			
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
		
	}

}
