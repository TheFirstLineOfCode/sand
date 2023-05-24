package com.thefirstlineofcode.sand.client.remoting;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;

public interface IRemoting {
	void registerActions(Class<?>... actionTypes);
	void registerActions(List<Class<?>> actionTypes);
	void execute(JabberId target, Object action);
	void execute(JabberId target, Object action, int timeout);
	void execute(JabberId target, Object action, Callback callback);
	void execute(JabberId target, Object action, int timeout, Callback callback);
	void execute(JabberId target, Execution execution);
	void execute(JabberId target, Execution execution, int timeout);
	void execute(JabberId target, Execution execution, Callback callback);
	void execute(JabberId target, Execution execution, int timeout, Callback callback);
	
	public interface Callback {
		void executed(Object xep);
		void occurred(StanzaError error);
		void timeout();
	}
}
