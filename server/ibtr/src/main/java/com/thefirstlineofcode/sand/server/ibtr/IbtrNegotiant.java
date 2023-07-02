package com.thefirstlineofcode.sand.server.ibtr;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.thefirstlineofcode.basalt.oxm.OxmService;
import com.thefirstlineofcode.basalt.oxm.parsers.core.stanza.IqParserFactory;
import com.thefirstlineofcode.basalt.oxm.parsing.IParsingFactory;
import com.thefirstlineofcode.basalt.oxm.translating.ITranslatingFactory;
import com.thefirstlineofcode.basalt.oxm.translators.core.stanza.IqTranslatorFactory;
import com.thefirstlineofcode.basalt.oxm.translators.core.stream.StreamTranslatorFactory;
import com.thefirstlineofcode.basalt.oxm.translators.error.StanzaErrorTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.InternalServerError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stream.Feature;
import com.thefirstlineofcode.basalt.xmpp.core.stream.Stream;
import com.thefirstlineofcode.granite.framework.core.connection.IClientConnectionContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.IMessage;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.InitialStreamNegotiant;
import com.thefirstlineofcode.sand.protocols.ibtr.ThingRegister;
import com.thefirstlineofcode.sand.protocols.ibtr.oxm.ThingRegisterParserFactory;
import com.thefirstlineofcode.sand.protocols.ibtr.oxm.ThingRegisterTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.thing.UnregisteredThing;
import com.thefirstlineofcode.sand.server.things.ThingRegistered;

public class IbtrNegotiant extends InitialStreamNegotiant {
	public static final Object KEY_IBTR_REGISTERED = new Object();
	
	private static final IParsingFactory parsingFactory = OxmService.createParsingFactory();
	private static final ITranslatingFactory translatingFactory = OxmService.createTranslatingFactory();
	
	static {
		parsingFactory.register(
				new IqProtocolChain(),
				new IqParserFactory()
		);
		parsingFactory.register(
				new IqProtocolChain(ThingRegister.PROTOCOL),
				new ThingRegisterParserFactory()
		);
		
		translatingFactory.register(
				Iq.class,
				new IqTranslatorFactory()
		);
		translatingFactory.register(
				ThingRegister.class,
				new ThingRegisterTranslatorFactory()
		);
		translatingFactory.register(StanzaError.class, new StanzaErrorTranslatorFactory());
		translatingFactory.register(Stream.class, new StreamTranslatorFactory());
	}
	
	private IThingRegistrar registrar;
	
	public IbtrNegotiant(String domainName, List<Feature> features, IThingRegistrar registrar) {
		super(domainName, features);
		
		this.registrar = registrar;
	}
	
	protected boolean doNegotiate(IClientConnectionContext context, IMessage message) {
		if (context.getAttribute(IbtrNegotiant.KEY_IBTR_REGISTERED) != null) {
			if (next != null) {
				done = true;
				return next.negotiate(context, message);
			}
			
			throw new ProtocolException(new BadRequest("Stream has estabilished."));
		}
		
		Iq iq = null;
		try {
			iq = (Iq)parsingFactory.parse((String)message.getPayload());
		} catch (Exception e) {
			// ignore
		}
		
		if (iq == null) {
			if (next != null) {
				done = true;
				return next.negotiate(context, message);
			}
			
			throw new ProtocolException(new BadRequest("Stream has estabilished."));
		}
		
		try {
			negotiateIbtr(context, message);
			return true;
		} catch (RuntimeException e) {
			throw e;
		}
		
	}

	private void negotiateIbtr(final IClientConnectionContext context, IMessage message) {
		Iq iq = (Iq)parsingFactory.parse((String)message.getPayload());
		
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		ThingRegister thingRegister = iq.getObject();
		if (thingRegister == null) {
			throw new ProtocolException(new BadRequest("Null register object."));
		}
		
		try {
			Object register = thingRegister.getRegister();
			if (register == null || !(register instanceof UnregisteredThing))
				throw new ProtocolException(new BadRequest("Register object isn't a string."));
			
			UnregisteredThing unregisteredThing = (UnregisteredThing)register;
			ThingRegistered registered = registrar.register(unregisteredThing.getThingId(),
					unregisteredThing.getRegistrationKey());
			Iq result = new Iq(Iq.Type.RESULT, iq.getId());
			result.setObject(new ThingRegister(registered.registeredThing));
			
			context.write(translatingFactory.translate(result));
		} catch (RuntimeException e) {
			// Standard client message processor doesn't support processing stanza error in normal situation.
			// So we process the exception by self.
			processException(iq, e);
		}
	}

	private void processException(Iq iq, RuntimeException e) {
		ProtocolException pe = null;
		
		if (e instanceof ProtocolException) {
			pe = (ProtocolException)e;
		} else {
			pe = findProtocolException(e);
		}
		
		if (pe != null) {
			if (pe.getError() instanceof StanzaError) {
				StanzaError error = (StanzaError)pe.getError();
				error.setId(iq.getId());
			}
			
			throw pe;
		}
		
		StanzaError error = new InternalServerError("Unexpected error. Error message: " + e.getMessage());
		error.setId(iq.getId());
		
		throw new ProtocolException(error);
	}

	private ProtocolException findProtocolException(Throwable t) {
		while (t.getCause() != null) {
			t = t.getCause();
			
			if (t instanceof ProtocolException)
				return (ProtocolException)t;
			
			if (t instanceof InvocationTargetException) {
				t = ((InvocationTargetException)t).getTargetException();
				if (t instanceof ProtocolException)
					return (ProtocolException)t;
				
				return new ProtocolException(new InternalServerError(t.getMessage()));
			}
		}
		
		return null;
	}
}
