package com.thefirstlineofcode.sand.server.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirer;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirerAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.protocols.operator.ConfirmConcentration;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmationDelegator;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmed;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmedEvent;

public class ConcentrationConfirmationProcessor implements IXepProcessor<Iq, ConfirmConcentration>,
		IEventFirerAware, IServerConfigurationAware {
	private Logger logger = LoggerFactory.getLogger(ConcentrationConfirmationProcessor.class);
	
	@Dependency("node.confirmation.delegator")
	private NodeConfirmationDelegator confirmationDelegator;
	
	private IEventFirer eventFirer;
	private String domainName;
	
	@Override
	public void process(IProcessingContext context, Iq iq, ConfirmConcentration xep) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		NodeConfirmed nodeConfirmed = confirmationDelegator.confirm(xep.getConcentratorThingName(), xep.getNodeThingId());
		if (logger.isInfoEnabled())
			logger.info("Thing which's thing ID is '{}' has been confirmed to be added to concentrator which's thing name is {}.",
					nodeConfirmed.getNodeAdded().getNodeThingId(), nodeConfirmed.getNodeAdded().getConcentratorThingName());
		
		Iq resultToConcentrator = new Iq(Iq.Type.RESULT, nodeConfirmed.getRequestId());
		JabberId jidConcentrator = getConcentratorJid(xep.getConcentratorThingName());
		resultToConcentrator.setTo(jidConcentrator);
		resultToConcentrator.setObject(nodeConfirmed.getNodeAdded());
		context.write(jidConcentrator, resultToConcentrator);
		
		Iq resultToOperator = Iq.createResult(iq);
		resultToOperator.setObject(nodeConfirmed.getNodeAdded());
		context.write(context.getJid(), resultToOperator);
		
		eventFirer.fire(new NodeConfirmedEvent(nodeConfirmed));
	}

	private JabberId getConcentratorJid(String concentratorThingName) {
		return new JabberId(concentratorThingName, domainName,
				Integer.toString(IConcentrator.LAN_ID_CONCENTRATOR));
	}

	@Override
	public void setEventFirer(IEventFirer eventFirer) {
		this.eventFirer = eventFirer;
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		this.domainName = serverConfiguration.getDomainName();
	}
}
