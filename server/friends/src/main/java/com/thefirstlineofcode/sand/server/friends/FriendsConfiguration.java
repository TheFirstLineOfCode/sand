package com.thefirstlineofcode.sand.server.friends;

import org.pf4j.Extension;
import org.springframework.context.annotation.Configuration;

import com.thefirstlineofcode.granite.framework.adf.core.ISpringConfiguration;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.sand.server.notification.INotificationDispatcher;

@Extension
@Configuration
public class FriendsConfiguration implements ISpringConfiguration, IApplicationComponentServiceAware {
	@Dependency(INotificationDispatcher.NAME_APP_COMPONENT_NOTIFICATION_DISPATCHER)
	private INotificationDispatcher notificationDispatcher;
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		notificationDispatcher.addNotificationListener(appComponentService.inject(new FollowDeliverer()));
	}
}
