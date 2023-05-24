package com.thefirstlineofcode.sand.server.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.protocols.operator.ConfirmConcentration;
import com.thefirstlineofcode.sand.server.concentrator.NodeAdditionDelegator;

public class ConcentrationConfirmationProcessor implements IXepProcessor<Iq, ConfirmConcentration> {
	private Logger logger = LoggerFactory.getLogger(ConcentrationConfirmationProcessor.class);
	
	@Dependency("node.addition.delegator")
	private NodeAdditionDelegator nodeAdditionDelegator;
	
	@Override
	public void process(IProcessingContext context, Iq iq, ConfirmConcentration xep) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		nodeAdditionDelegator.confirm(xep.getConcentratorThingName(), xep.getNodeThingId());
		
		if (logger.isInfoEnabled())
			logger.info("Thing which's thing ID is '{}' has been confirmed to be added to concentrator which's thing name is {}.",
					xep.getNodeThingId(), xep.getConcentratorThingName());
		
		context.write(context.getJid(), new Iq(Iq.Type.RESULT, iq.getId()));
	}

}
