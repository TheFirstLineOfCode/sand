package com.thefirstlineofcode.sand.server.notification;


import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;

public interface INotificationListener {
	void notified(IProcessingContext context, Iq iq, JabberId notifier, Notification notification);
}
