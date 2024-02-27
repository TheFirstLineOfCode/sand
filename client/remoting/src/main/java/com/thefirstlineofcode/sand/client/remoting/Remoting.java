package com.thefirstlineofcode.sand.client.remoting;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;

public class Remoting implements IRemoting {
	private static final int DEFAULT_TIMEOUT = 10 * 1000;
	
	private IChatServices chatServices;
	
	@Override
	public void execute(JabberId target, Object action) {
		execute(target, action, null);
	}
	
	@Override
	public void execute(JabberId target, Object action, int timeout) {
		execute(target, action, timeout, null);
	}
	
	@Override
	public void execute(JabberId target, Object action, Callback callback) {
		execute(target, action, DEFAULT_TIMEOUT, callback);
	}
	
	@Override
	public void execute(JabberId target, Object action, int timeout, Callback callback) {
		Execution execution = new Execution(action);
		
		if (target.getResource() != null &&
				!RegisteredEdgeThing.DEFAULT_RESOURCE_NAME.equals(target.getResource())) {
			execution.setLanTraceable(true);
		}
		
		execute(target, execution, timeout, callback);
	}

	@Override
	public void execute(JabberId target, Execution execution) {
		execute(target, execution, null);
	}
	
	@Override
	public void execute(JabberId target, Execution execution, int timeout) {
		execute(target, execution, timeout, null);
	}
	
	@Override
	public void execute(JabberId target, Execution execution, Callback callback) {
		execute(target, execution, DEFAULT_TIMEOUT, callback);
	}
	
	@Override
	public void execute(JabberId target, Execution execution, int timeout, Callback callback) {
		chatServices.getTaskService().execute(new ExecutionTask(target, execution, timeout, callback));
	}
	
	private class ExecutionTask implements ITask<Iq> {
		private JabberId target;
		private Execution execution;
		private int timeout;
		private Callback callback;
		
		public ExecutionTask(JabberId target, Execution execution, int timeout, Callback callback) {
			this.target = target;
			this.execution = execution;
			this.timeout = timeout;
			this.callback = callback;
		}
		
		@Override
		public void trigger(IUnidirectionalStream<Iq> stream) {
			Iq iq = new Iq(Iq.Type.SET, execution, Stanza.generateId("exec"));
			iq.setTo(target);
			
			if (execution.isLanTraceable() && execution.getLanTimeout() != null)
				timeout += execution.getLanTimeout();
			
			stream.send(iq, timeout);
		}

		@Override
		public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
			if (callback != null) {
				callback.executed(iq.getObject());
			}
		}
		
		@Override
		public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
			if (callback != null) {
				callback.occurred(error);
				return true;
			}
			
			return false;
		}
		
		@Override
		public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
			if (callback != null) {
				callback.timeout();
				return true;
			}
			
			return false;
		}

		@Override
		public void interrupted() {}
		
	}
	
	@Override
	public void registerActions(Class<?>... actionTypes) {
		for (Class<?> actionType : actionTypes) {			
			ProtocolObject protocolObject = actionType.getAnnotation(ProtocolObject.class);
			if (protocolObject == null)
				throw new RuntimeException(
						String.format("Illegal action type. An action type must be a protocol object. Action type name: %s.",
								actionType.getName()));
			
			chatServices.getOxmFactory().register(actionType, new CocTranslatorFactory<>(actionType));
			chatServices.getOxmFactory().register(
					new IqProtocolChain(Execution.PROTOCOL).
					next(new Protocol(protocolObject.namespace(), protocolObject.localName())),
					new CocParserFactory<>(actionType));
			chatServices.getOxmFactory().register(new IqProtocolChain(
					new Protocol(protocolObject.namespace(), protocolObject.localName())),
					new CocParserFactory<>(actionType));
		}
	}

	@Override
	public void registerActions(List<Class<?>> actionTypes) {
		registerActions(actionTypes.toArray(new Class<?>[0]));
	}
	
}
