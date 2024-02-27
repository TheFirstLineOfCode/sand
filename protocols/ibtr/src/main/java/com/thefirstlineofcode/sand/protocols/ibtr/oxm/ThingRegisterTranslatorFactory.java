package com.thefirstlineofcode.sand.protocols.ibtr.oxm;

import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.ibtr.ThingRegister;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;
import com.thefirstlineofcode.sand.protocols.thing.UnregisteredEdgeThing;

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
			
			if (register instanceof UnregisteredEdgeThing) {
				UnregisteredEdgeThing unregisteredEdgeThing = (UnregisteredEdgeThing)register;
				writer.writeElementBegin("unregistered-edge-thing").
				writeTextOnly("thing-id", unregisteredEdgeThing.getThingId()).
				writeTextOnly("registration-key", unregisteredEdgeThing.getRegistrationCode()).
				writeElementEnd();
			} else if (register instanceof RegisteredEdgeThing) {
				RegisteredEdgeThing registeredEdgeThing = (RegisteredEdgeThing)register;
				writer.writeElementBegin("registered-edge-thing").
					writeTextOnly("thing-name", registeredEdgeThing.getThingName()).
					writeTextOnly("credentials", registeredEdgeThing.getCredentials()).
					writeTextOnly("security-key", BinaryUtils.encodeToBase64(registeredEdgeThing.getSecretKey())).					
				writeElementEnd();
			} else {
				throw new RuntimeException("Unknown register object.");
			}
			
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
		
	}

}
