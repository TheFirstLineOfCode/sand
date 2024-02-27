package com.thefirstlineofcode.sand.server.friends;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;
import com.thefirstlineofcode.sand.server.notification.INotificationListener;

public class FollowDeliverer implements INotificationListener, IConfigurationAware {
	private static final String CONFIGURATION_KEY_DISABLE_LAN_FOLLOW_DELIVERY = "disable.lan.follow.delivery";
	
	private static final Logger logger = LoggerFactory.getLogger(FollowDeliverer.class);
	
	private boolean disableLanFollowDelivery;
	
	@BeanDependency
	private IFriendsManager friendsManager;
	
	@BeanDependency
	private IResourcesService resourcesService;

	@Override
	public void notified(IProcessingContext context, Iq iq, JabberId notifier, Notification notification) {
		Object event = notification.getEvent();
		ProtocolObject pObj = event.getClass().getAnnotation(ProtocolObject.class);
		if (pObj == null)
			throw new RuntimeException("Isn't event a protocol object?");
		
		List<JabberId> followers = friendsManager.getFollowers(notifier, new Protocol(pObj.namespace(), pObj.localName()));
		if (followers == null || followers.isEmpty())
			return;
		
		for (JabberId follower : followers) {
			// Follow will be delivered by concentrator in LAN.
			if (!isLanFollowDeliveryDisabled() && isLanFollow(notifier, follower))
				continue;
			
			JabberId edgeTarget = new JabberId(follower.getNode(), follower.getDomain(), RegisteredEdgeThing.DEFAULT_RESOURCE_NAME);
			IResource resource = resourcesService.getResource(edgeTarget);
			if (resource == null) {
				if (logger.isWarnEnabled())
					logger.warn("Can't deliver follow because edge target '{}' not being online.", edgeTarget);
				return;
			}
			
			Notification followNotification = new Notification(notification.getEvent(), false);
			Iq followForward = new Iq(Iq.Type.SET, followNotification);
			followForward.setFrom(notifier);
			followForward.setTo(follower);
			
			context.write(edgeTarget, followForward);
		}
	}

	private boolean isLanFollow(JabberId notifier, JabberId follower) {
		return notifier.getBareId().equals(follower.getBareId());
	}

	private boolean isLanFollowDeliveryDisabled() {
		return disableLanFollowDelivery;
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		disableLanFollowDelivery = configuration.getBoolean(CONFIGURATION_KEY_DISABLE_LAN_FOLLOW_DELIVERY, false);
	}
}
