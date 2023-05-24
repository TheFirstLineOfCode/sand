package com.thefirstlineofcode.sand.protocols.ibtr.oxm;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.Attribute;
import com.thefirstlineofcode.basalt.oxm.Value;
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
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;

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
			} else if (parsingPath.match("thing-id")) {
				return new ElementParserAdaptor<ThingRegister>() {
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						if (context.getObject().getRegister() != null)
							throw new ProtocolException(new BadRequest("Thing registration document allows only one subelement."));
						
						context.getObject().setRegister(text.getString());
					};
				};
			} else if (parsingPath.match("/thing-identity")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processAttributes(IParsingContext<ThingRegister> context, List<Attribute> attributes) {
						if (context.getObject().getRegister() != null)
							throw new ProtocolException(new BadRequest("Thing registration document allows only one subelement."));
						
						context.getObject().setRegister(new ThingIdentity());
					}
				};
			} else if (parsingPath.match("/thing-identity/thing-name")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						ThingIdentity identity = (ThingIdentity)context.getObject().getRegister();
						identity.setThingName(text.getString());
					}
				};
			} else if (parsingPath.match("/thing-identity/credentials")) {
				return new ElementParserAdaptor<ThingRegister>() {
					@Override
					public void processText(IParsingContext<ThingRegister> context, Value<?> text) {
						ThingIdentity identity = (ThingIdentity)context.getObject().getRegister();
						identity.setCredentials(text.getString());
					}
				};
			} else {
				return super.getElementParser(parsingPath);
			}
		}
	}
}
