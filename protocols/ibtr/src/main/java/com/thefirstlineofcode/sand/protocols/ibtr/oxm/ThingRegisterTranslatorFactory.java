package com.thefirstlineofcode.sand.protocols.ibtr.oxm;

import com.thefirstlineofcode.basalt.oxm.Value;
import com.thefirstlineofcode.basalt.oxm.translating.IProtocolWriter;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.ibtr.ThingRegister;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

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
			
			if (register instanceof String) {
				writer.writeElementBegin("thing-id").writeText(Value.create((String)register)).writeElementEnd();
			} else if (register instanceof ThingIdentity) {
				ThingIdentity thingIdentity = (ThingIdentity)register;
				writer.writeElementBegin("thing-identity").
					writeTextOnly("thing-name", thingIdentity.getThingName()).
					writeTextOnly("credentials", thingIdentity.getCredentials()).
				writeElementEnd();
			} else {
				throw new RuntimeException("Unknown register object.");
			}
			
			writer.writeProtocolEnd();
			
			return writer.getDocument();
		}
		
	}

}
