package com.thefirstlineofcode.sand.client.thing;

import java.util.Properties;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanNotification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.oxm.LanNotificationParserFactory;
import com.thefirstlineofcode.sand.protocols.thing.tacp.oxm.NotificationParserFactory;
import com.thefirstlineofcode.sand.protocols.thing.tacp.oxm.NotificationTranslatorFactory;

public class ThingPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(new IqProtocolChain(LanNotification.PROTOCOL),
				new LanNotificationParserFactory());
		chatSystem.registerParser(new IqProtocolChain(Notification.PROTOCOL),
				new NotificationParserFactory());
		chatSystem.registerTranslator(Notification.class,
				new NotificationTranslatorFactory());
		chatSystem.registerApi(INotificationService.class, NotificationService.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(INotificationService.class);
		chatSystem.unregisterTranslator(Notification.class);
		chatSystem.unregisterParser(new IqProtocolChain(Notification.PROTOCOL));
		chatSystem.unregisterParser(new IqProtocolChain(LanNotification.PROTOCOL));
	}
}
