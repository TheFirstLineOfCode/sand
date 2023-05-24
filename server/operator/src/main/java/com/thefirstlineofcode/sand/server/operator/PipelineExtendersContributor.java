package com.thefirstlineofcode.sand.server.operator;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.sand.protocols.operator.ApproveFollow;
import com.thefirstlineofcode.sand.protocols.operator.AuthorizeThing;
import com.thefirstlineofcode.sand.protocols.operator.ConfirmConcentration;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	private static final IqProtocolChain AUTHORIZE_THING_PROTOCOL_CHAIN = new IqProtocolChain(AuthorizeThing.PROTOCOL);
	private static final IqProtocolChain CONFIRM_CONCENTRATION_PROTOCOL_CHAIN = new IqProtocolChain(ConfirmConcentration.PROTOCOL);
	private static final IqProtocolChain APPROVE_FOLLOW_PROTOCOL_CHAIN = new IqProtocolChain(ApproveFollow.PROTOCOL);

	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.
			registerCocParser(AUTHORIZE_THING_PROTOCOL_CHAIN, AuthorizeThing.class).
			registerSingletonXepProcessor(AUTHORIZE_THING_PROTOCOL_CHAIN, new ThingAuthorizationProcessor()).
			registerCocParser(CONFIRM_CONCENTRATION_PROTOCOL_CHAIN, ConfirmConcentration.class).
			registerSingletonXepProcessor(CONFIRM_CONCENTRATION_PROTOCOL_CHAIN, new ConcentrationConfirmationProcessor()).
			registerCocParser(APPROVE_FOLLOW_PROTOCOL_CHAIN, ApproveFollow.class).
			registerSingletonXepProcessor(APPROVE_FOLLOW_PROTOCOL_CHAIN, new FollowApprovalProcessor());
	}
}
