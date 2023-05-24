package com.thefirstlineofcode.sand.server.stream;

import com.thefirstlineofcode.granite.framework.adf.core.AdfComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.auth.IAuthenticator;
import com.thefirstlineofcode.granite.pipeline.stages.stream.IStreamNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.InitialStreamNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.SaslNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.SessionEstablishmentNegotiant;
import com.thefirstlineofcode.granite.pipeline.stages.stream.negotiants.TlsNegotiant;
import com.thefirstlineofcode.granite.stream.standard.StandardClientMessageProcessor;

public class ThingClientMessageProcessor extends StandardClientMessageProcessor {
	private static final String BEAN_NAME_THING_AUTHENTICATOR = "thingAuthenticator";
	
	protected IStreamNegotiant createNegotiant() {
		IStreamNegotiant intialStream = new InitialStreamNegotiant(hostName,
				getInitialStreamNegotiantAdvertisements());
		
		IStreamNegotiant tls = new TlsNegotiant(hostName, tlsRequired,
				getTlsNegotiantAdvertisements());
		
		IStreamNegotiant sasl = new SaslNegotiant(hostName,
				saslSupportedMechanisms, saslAbortRetries, saslFailureRetries,
				getSaslNegotiantFeatures(), authenticator);
		
		IStreamNegotiant resourceBinding = new ThingResourceBindingNegotiant(connectionManager,
				hostName, sessionManager, router);
		IStreamNegotiant sessionEstablishment = new SessionEstablishmentNegotiant(router,
				sessionManager, eventFirer, sessionListenerDelegate);
		
		resourceBinding.setNext(sessionEstablishment);
		sasl.setNext(resourceBinding);
		tls.setNext(sasl);
		intialStream.setNext(tls);
		
		return intialStream;
	}
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
		authenticator = ((AdfComponentService)appComponentService).getApplicationContext().
				getBean(BEAN_NAME_THING_AUTHENTICATOR, IAuthenticator.class);
	}
}
