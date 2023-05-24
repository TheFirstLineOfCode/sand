package com.thefirstlineofcode.sand.server.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Forbidden;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.protocols.operator.ApproveFollow;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.friends.Follow;
import com.thefirstlineofcode.sand.server.friends.IFriendsManager;
import com.thefirstlineofcode.sand.server.friends.ReduplicateFollowException;
import com.thefirstlineofcode.sand.server.location.ILocationService;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class FollowApprovalProcessor implements IXepProcessor<Iq, ApproveFollow>  {
	private Logger logger = LoggerFactory.getLogger(FollowApprovalProcessor.class);
	
	@BeanDependency
	private IAccountManager accountManager;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@BeanDependency
	private ILocationService locationService;
		
	@BeanDependency
	private IFriendsManager friendsManager;

	@Override
	public void process(IProcessingContext context, Iq iq, ApproveFollow approveFollow) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		String approver = context.getJid().getNode();
		if (!accountManager.exists(approver))
			throw new ProtocolException(new Forbidden(String.format("'%s' isn't a user. Are you a thing?", approver)));
		
		if (approveFollow.getEvent() == null)
			throw new ProtocolException(new BadRequest("Null event."));
		
		if (approveFollow.getFriend() == null)
			throw new ProtocolException(new BadRequest("Null friend."));
		
		String friendThingId = locationService.getThingIdByJid(approveFollow.getFriend());
		if (friendThingId == null)
			throw new ProtocolException(new ItemNotFound(String.format("'%s' not a thing.", approveFollow.getFriend().toString())));
		
		String friendModel = thingManager.getModel(friendThingId);
		if (!thingManager.isEventSupported(friendModel, approveFollow.getEvent())) {
			throw new ProtocolException(new ServiceUnavailable(
					String.format("Event '%s' isn't supported by thing which's model is '%s'.", approveFollow.getEvent(), friendModel)));
		}
		
		if (approveFollow.getFollower() == null)
			throw new ProtocolException(new BadRequest("Null follower."));
		
		String followerThingId = locationService.getThingIdByJid(approveFollow.getFollower());
		if (followerThingId == null)
			throw new ProtocolException(new ItemNotFound(String.format("'%s' not a thing.", approveFollow.getFollower().toString())));
		
		String followerModel = thingManager.getModel(followerThingId);
		if (!thingManager.isEventFollowed(thingManager.getModel(followerThingId), approveFollow.getEvent())) {
			throw new ProtocolException(new NotAcceptable(
					String.format("Event '%s' isn't followed by thing which's model is '%s'.", approveFollow.getEvent(), followerModel)));
		}
		
		if (approveFollow.getFollower().equals(approveFollow.getFriend()))
			throw new ProtocolException(new BadRequest("Do you want to follow yourself?"));
		
		try {
			friendsManager.approve(new Follow(approveFollow.getFriend(), approveFollow.getEvent(),
					approveFollow.getFollower()), approver);
			
			if (logger.isInfoEnabled())
				logger.info("'{}' has been approved to follow '{} - {}'.", approveFollow.getFollower(),
						approveFollow.getFriend(), approveFollow.getEvent());
		} catch (ReduplicateFollowException e) {
			throw new ProtocolException(new Conflict());
		}
		
		context.write(Iq.createResult(iq));
	}
}
