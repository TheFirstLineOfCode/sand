package com.thefirstlineofcode.sand.server.ibtr;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.stream.Feature;
import com.thefirstlineofcode.basalt.xmpp.core.stream.Features;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.annotations.Component;
import com.thefirstlineofcode.granite.framework.core.connection.IClientConnectionContext;
import com.thefirstlineofcode.granite.pipeline.stages.stream.IStreamNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.InitialStreamNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.ResourceBindingNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.SaslNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.SessionEstablishmentNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.TlsNegotiant;
import com.thefirstlineofcode.sand.protocols.ibtr.Register;
import com.thefirstlineofcode.sand.protocols.ibtr.oxm.RegisterTranslatorFactory;
import com.thefirstlineofcode.sand.server.stream.ThingClientMessageProcessor;

@Component("ibtr.supported.client.message.processor")
public class IbtrSupportedClientMessageProcessor extends ThingClientMessageProcessor {
	private static final String APP_COMPONENT_NAME_EDGE_THING_REGISTRAR = "edge.thing.registrar";
	
	private IEdgeThingRegistrar registrar;
	
	@Override
	protected IStreamNegotiant createNegotiant() {
		if (tlsRequired) {
			IStreamNegotiant initialStream = new InitialStreamNegotiant(hostName,
					getInitialStreamNegotiantAdvertisements());
			
			IStreamNegotiant tls = new IbtrSupportedTlsNegotiant(hostName, tlsRequired,
					getTlsNegotiantAdvertisements());
			
			IStreamNegotiant ibtrAfterTls = new IbtrNegotiant(hostName, getTlsNegotiantAdvertisements(),
					registrar);
			
			IStreamNegotiant sasl = new SaslNegotiant(hostName,
					saslSupportedMechanisms, saslAbortRetries, saslFailureRetries,
					getSaslNegotiantFeatures(), authenticator);
			
			IStreamNegotiant resourceBinding = new ResourceBindingNegotiant(connectionManager,
					hostName, sessionManager, router);
			IStreamNegotiant sessionEstablishment = new SessionEstablishmentNegotiant(router,
					sessionManager, eventFirer, sessionListenerDelegate);
			
			resourceBinding.setNext(sessionEstablishment);
			sasl.setNext(resourceBinding);
			ibtrAfterTls.setNext(sasl);
			tls.setNext(ibtrAfterTls);
			initialStream.setNext(tls);
			
			return initialStream;
		} else {
			IStreamNegotiant initialStream = new IbtrSupportedInitialStreamNegotiant(hostName,
					getInitialStreamNegotiantAdvertisements());
			
			IStreamNegotiant ibtrBeforeTls = new IbtrNegotiant(hostName, getInitialStreamNegotiantAdvertisements(),
					registrar);
			
			IStreamNegotiant tls = new IbtrSupportedTlsNegotiant(hostName, tlsRequired,
					getTlsNegotiantAdvertisements());
			
			IStreamNegotiant ibtrAfterTls = new IbtrNegotiant(hostName, getTlsNegotiantAdvertisements(),
					registrar);
			
			IStreamNegotiant sasl = new SaslNegotiant(hostName,
					saslSupportedMechanisms, saslAbortRetries, saslFailureRetries,
					getSaslNegotiantFeatures(), authenticator);
			
			IStreamNegotiant resourceBinding = new ResourceBindingNegotiant(connectionManager,
					hostName, sessionManager, router);
			IStreamNegotiant sessionEstablishment = new SessionEstablishmentNegotiant(router,
					sessionManager, eventFirer, sessionListenerDelegate);
			
			resourceBinding.setNext(sessionEstablishment);
			sasl.setNext(resourceBinding);
			ibtrAfterTls.setNext(sasl);
			tls.setNext(ibtrAfterTls);
			ibtrBeforeTls.setNext(tls);
			initialStream.setNext(ibtrBeforeTls);
			
			return initialStream;
		}
		
	}
	
	private static class IbtrSupportedInitialStreamNegotiant extends InitialStreamNegotiant {
		static {
			oxmFactory.register(Register.class, new RegisterTranslatorFactory());
		}
		
		public IbtrSupportedInitialStreamNegotiant(String domainName, List<Feature> features) {
			super(domainName, features);
			features.add(new Register());
		}
	}
	
	private static class IbtrSupportedTlsNegotiant extends TlsNegotiant {
		static {
			oxmFactory.register(Register.class, new RegisterTranslatorFactory());
		}

		public IbtrSupportedTlsNegotiant(String domainName, boolean tlsRequired, List<Feature> features) {
			super(domainName, tlsRequired, features);
		}
		
		@Override
		protected Features getAvailableFeatures(IClientConnectionContext context) {
			Features features = super.getAvailableFeatures(context);
			
			if (context.getAttribute(IbtrNegotiant.KEY_IBTR_REGISTERED) == null) {
				features.getFeatures().add(new Register());
			}
			
			return features;
		}
	}
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		super.setApplicationComponentService(appComponentService);
		
		registrar = appComponentService.getAppComponent(APP_COMPONENT_NAME_EDGE_THING_REGISTRAR, IEdgeThingRegistrar.class);
	}
}
