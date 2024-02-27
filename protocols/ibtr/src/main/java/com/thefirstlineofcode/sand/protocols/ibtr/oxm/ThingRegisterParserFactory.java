package com.thefirstlineofcode.sand.protocols.ibtr.oxm;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Value;
import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.parsing.ElementParserAdaptor;
import com.thefirstlineofcode.basalt.oxm.parsing.IElementParser;
import com.thefirstlineofcode.basalt.oxm.parsing.IParser;
import com.thefirstlineofcode.basalt.oxm.parsing.IParserFactory;
import com.thefirstlineofcode.basalt.oxm.parsing.IParsingContext;
import com.thefirstlineofcode.basalt.oxm.parsing.IParsingPath;
import com.thefirstlineofcode.basalt.oxm.parsing.ParserAdaptor;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.sand.protocols.ibtr.ThingRegister;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;
import com.thefirstlineofcode.sand.protocols.thing.UnregisteredEdgeThing;

public class ThingRegisterParserFactory implements IParserFactory<ThingRegister> {
	@Override
	public Protocol getProtocol() {
		return ThingRegister.PROTOCOL;
	}

	@Override
	public IParser<ThingRegister> create() {
		return new ThingRegisterParser();
	}
	
	private static class ThingRegisterParser extends ParserAdaptor<ThingRegister> {
		public ThingRegisterParser() {
			super(ThingRegister.class);
		}
		
		@Override
		public IElementParser<ThingRegister> getElementParser(IParsingPath parsingPath) {
			if (parsingPath.match("/")) {
				return new ElementParserAdaptor<>();
			} else if (parsingPath.match("unregistered-edge-thing")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processAttributes(IParsingContext<ThingRegister> context, List<Attribute> attributes) {
						super.processAttributes(context, attributes);
						
						if (context.getObject().getRegister() != null)
							throw new ProtocolException(new BadRequest("Thing registration document allows only one subelement."));
						
						context.getObject().setRegister(new UnregisteredEdgeThing());
					}
				};
			} else if (parsingPath.match("/unregistered-edge-thing/thing-id")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						UnregisteredEdgeThing unregisteredEdgeThing = (UnregisteredEdgeThing)context.getObject().getRegister();
						unregisteredEdgeThing.setThingId(text.getString());
					}
				};
			} else if (parsingPath.match("/unregistered-edge-thing/registration-key")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						UnregisteredEdgeThing unregisteredEdgeThing = (UnregisteredEdgeThing)context.getObject().getRegister();
						unregisteredEdgeThing.setRegistrationCode(text.getString());
					}
				};
			} else if (parsingPath.match("/registered-edge-thing")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processAttributes(IParsingContext<ThingRegister> context, List<Attribute> attributes) {
						super.processAttributes(context, attributes);
						
						if (context.getObject().getRegister() != null)
							throw new ProtocolException(new BadRequest("Thing registration document allows only one subelement."));
						
						context.getObject().setRegister(new RegisteredEdgeThing());
					}
				};
			} else if (parsingPath.match("//registered-edge-thing/thing-name")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						RegisteredEdgeThing registeredEdgeThing = (RegisteredEdgeThing)context.getObject().getRegister();
						registeredEdgeThing.setThingName(text.getString());
					}
				};
			} else if (parsingPath.match("//registered-edge-thing/credentials")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						RegisteredEdgeThing registeredEdgeThing = (RegisteredEdgeThing)context.getObject().getRegister();
						registeredEdgeThing.setCredentials(text.getString());
					}
				};
			} else if (parsingPath.match("//registered-edge-thing/security-key")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						RegisteredEdgeThing registeredEdgeThing = (RegisteredEdgeThing)context.getObject().getRegister();
						registeredEdgeThing.setSecretKey(BinaryUtils.decodeFromBase64(text.getString()));
					}
				};
			} else {
				return super.getElementParser(parsingPath);
			}
		}
	}
}
