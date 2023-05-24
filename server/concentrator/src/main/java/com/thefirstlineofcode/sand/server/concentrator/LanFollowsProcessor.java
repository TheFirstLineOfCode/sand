package com.thefirstlineofcode.sand.server.concentrator;

import java.util.List;

import com.thefirstlinelinecode.sand.protocols.concentrator.friends.LanFollow;
import com.thefirstlinelinecode.sand.protocols.concentrator.friends.LanFollows;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Forbidden;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.server.friends.Follow;
import com.thefirstlineofcode.sand.server.friends.IFriendsManager;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class LanFollowsProcessor implements IXepProcessor<Iq, LanFollows>, IConfigurationAware {
	private static final String CONFIGURATION_KEY_DISABLE_LAN_FOLLOW_DELIVERY = "disable.lan.follow.delivery";
	
	private boolean disableLanFollowDelivery;

	
	@BeanDependency
	private IFriendsManager friendsManager;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@Override
	public void process(IProcessingContext context, Iq iq, LanFollows xep) {
		if (iq.getType() != Iq.Type.GET)
			throw new ProtocolException(new BadRequest("IQ type should be 'GET'."));
		
		JabberId jid = context.getJid();
		String model = thingManager.getModel(thingManager.getThingIdByThingName(jid.getNode()));
		if (!thingManager.getModelDescriptor(model).isConcentrator())
			throw new ProtocolException(new Forbidden("Not a concentrator."));
		
		JabberId from = iq.getFrom();
		if (from != null && !from.equals(jid))
			throw new ProtocolException(new BadRequest("Illegal 'from' attribute value."));
		
		if (disableLanFollowDelivery) {
			context.write(Iq.createResult(iq, new LanFollows()));
			return;
		}
		
		context.write(Iq.createResult(iq, getLanFollows(jid)));
	}

	private LanFollows getLanFollows(JabberId jid) {
		LanFollows lanFollows = new LanFollows();
		
		List<Follow> follows = friendsManager.getLanFollows(jid.getNode());
		if (follows != null) {
			for (Follow follow : follows) {
				lanFollows.getLanFollows().add(new LanFollow(Integer.parseInt(follow.getFriend().getResource()),
						follow.getEvent(), Integer.parseInt(follow.getFollower().getResource())));
			}
		}
		
		return lanFollows;
	}
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		disableLanFollowDelivery = configuration.getBoolean(CONFIGURATION_KEY_DISABLE_LAN_FOLLOW_DELIVERY, false);
	}
}
