package com.thefirstlineofcode.sand.client.thing;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;

public interface INotifier {
	void registerSupportedEvent(Class<?> eventType);
	
	void notify(Object event);
	void notify(JabberId notifier, Object event);
	void notifyWithAck(Object event);
	void notifyWithAck(JabberId notifier, Object event);
	void notifyWithAck(Object event, IRexStrategy rexStrategy);
	void notifyWithAck(JabberId notifier, Object event, IRexStrategy rexStrategy);
	void notifyWithAck(Object event, IAckListener ackListener);
	void notifyWithAck(JabberId notifier, Object event, IAckListener ackListener);
	void notifyWithAck(Object event, IRexStrategy rexStrategy, IAckListener ackListener);
	void notifyWithAck(JabberId notifier, Object event, IRexStrategy rexStrategy, IAckListener ackListener);
}
