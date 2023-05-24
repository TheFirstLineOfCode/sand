package com.thefirstlineofcode.sand.server.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.protocols.operator.AuthorizeThing;
import com.thefirstlineofcode.sand.server.ibtr.ThingAuthorizationDelegator;

public class ThingAuthorizationProcessor implements IXepProcessor<Iq, AuthorizeThing> {
	private Logger logger = LoggerFactory.getLogger(ThingAuthorizationProcessor.class);
	
	@Dependency("thing.authorization.delegator")
	private ThingAuthorizationDelegator thingAuthorizationDelegator;
	
	@Override
	public void process(IProcessingContext context, Iq iq, AuthorizeThing xep) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		thingAuthorizationDelegator.authorize(xep.getThingId(), context.getJid().getNode());
		
		if (logger.isInfoEnabled())
			logger.info("Thing '{}' has authorized by authorizer '{}'.", xep.getThingId(), context.getJid().getNode());
		
		context.write(context.getJid(), new Iq(Iq.Type.RESULT, iq.getId()));
	}

}
