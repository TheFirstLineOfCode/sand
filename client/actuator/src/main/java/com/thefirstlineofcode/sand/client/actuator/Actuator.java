package com.thefirstlineofcode.sand.client.actuator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.IOxmFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.InternalServerError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;

public class Actuator implements IActuator, IIqListener {
	private static final Logger logger = LoggerFactory.getLogger(Actuator.class);
	
	protected IChatServices chatServices;
	protected Map<Class<?>, IExecutorFactory<?>> executorFactories;
	protected IOxmFactory oxmFactory;
	protected JabberId host;
	protected boolean started;
	
	public Actuator(IChatServices chatServices) {
		this.chatServices = chatServices;
		executorFactories = new HashMap<>();

		oxmFactory = chatServices.getOxmFactory();
		started = false;
	}
	
	@Override
	public void received(Iq iq) {
		Execution execution = iq.getObject();
		
		if (logger.isInfoEnabled()) {
			logger.info("Received a execution message which's action object is {} from '{}'.",
					execution.getAction(), iq.getFrom() == null ? getHost() : iq.getFrom());
		}
		
		try {
			execute(iq, execution);
		} catch (ProtocolException e) {
			if (logger.isErrorEnabled())
				logger.error(String.format("Failed to execute the action %s which was sent from '%s' on thing '%s'.",
						execution.getAction(), iq.getFrom() == null ? getHost() : iq.getFrom(), iq.getTo()), e);
			
			throw e;
		} catch (RuntimeException e) {
			if (logger.isErrorEnabled())
				logger.error(String.format("Failed to execute the action %s which was sent from '%s' on thing '%s'.",
						execution.getAction(), iq.getFrom() == null ? getHost() : iq.getFrom()), e);
			
			throw new ProtocolException(new InternalServerError(), e);
		}
	}
	
	protected JabberId getHost() {
		if (host == null)
			host = JabberId.parse(chatServices.getStream().getStreamConfig().getHost());
		
		return host;
	}

	@SuppressWarnings("unchecked")
	protected <T> void execute(Iq iq, Execution execution) {
		JabberId from = iq.getFrom();
		if (from == null) {
			from = getHost();
		}
		
		T action = (T)execution.getAction();
		if (logger.isInfoEnabled()) {
			logger.info("Try to execute the action {} which was sent from '{}' on thing '{}'.", action, from, iq.getTo());
		}
		
		if (!executorFactories.containsKey(action.getClass())) {
			if (logger.isWarnEnabled()) {
				logger.warn("Action which's type is {} not supported by thing '{}'.",
						action.getClass().getName(), iq.getTo());
			}
			
			throw new ProtocolException(new ServiceUnavailable(String.format(
					"Action which's type is %s not supported by thing '%s'.",
					action.getClass().getName(), iq.getTo())));
		}
		
		Object resultObj = null;
		try {			
			IExecutor<T> executor = createExecutor(action);
			resultObj = executor.execute(iq, action);
		} catch (ProtocolException e) {
			logger.error("Failed to execute action which's type is '{}'.", action.getClass().getName(), e);
			throw e;
		} catch (RuntimeException e) {
			logger.error("Failed to execute action which's type is '{}'.", action.getClass().getName(), e);
			throw new ProtocolException(new InternalServerError(String.format("Failed to execute action which's type is '%s'.",
					action.getClass().getName())));
		}
		
		Iq result = Iq.createResult(iq, resultObj);
		chatServices.getIqService().send(result);
	}
	
	@SuppressWarnings("unchecked")
	private <T> IExecutor<T> createExecutor(T action) throws ProtocolException {
		IExecutorFactory<T> executorFactory = (IExecutorFactory<T>)executorFactories.get(action.getClass());
		if (executorFactory != null)
			return executorFactory.create();
		
		return null;
	}
	
	private IExecutor<?> createExecutor(Class<? extends IExecutor<?>> executorType) {
		IExecutor<?> executor = null;
		Constructor<?> constructor = null;
		try {
			constructor = (Constructor<?>)executorType.getConstructor(IChatServices.class);
			
			try {
				executor = (IExecutor<?>)constructor.newInstance(chatServices);
			} catch (Exception e) {
				throw new ProtocolException(new InternalServerError("Failed to create executor instance.", e));
			}
		} catch (NoSuchMethodException | SecurityException e) {
			constructor = createEmptyConstructor(executorType, constructor);
			try {
				executor = (IExecutor<?>)constructor.newInstance();
			} catch (Exception exception) {
				throw new ProtocolException(new InternalServerError("Failed to create executor instance.", exception));
			}
		}
		
		for (Field field : executorType.getFields()) {
			if (IChatServices.class.isAssignableFrom(field.getType())) {
				boolean oldAccesssiable = field.isAccessible();
				try {
					field.setAccessible(true);
					field.set(executor, chatServices);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new ProtocolException(new InternalServerError("Can't set chat services to executor.", e));
				} finally {
					field.setAccessible(oldAccesssiable);
				}
				
				break;
			}
		}
		
		return executor;
	}
	
	private Constructor<?> createEmptyConstructor(Class<?> executorType,
			Constructor<?> constructor) {
		try {
			constructor = (Constructor<?>)executorType.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ProtocolException(new InternalServerError(String.format(
					"Can't create executor for executor type: %s.", executorType.getName()), e));			
		}
		
		return constructor;
	}

	@Override
	public boolean unregisterExecutor(Class<?> actionType) {
		return executorFactories.remove(actionType) != null;
	}

	@Override
	public void start() {
		if (started)
			return;
		
		if (chatServices.getIqService().getListener(Execution.PROTOCOL) != this)
			chatServices.getIqService().addListener(Execution.PROTOCOL, this);
		
		started = true;
	}
	
	@Override
	public void stop() {
		if (!started)
			return;
		
		chatServices.getIqService().removeListener(Execution.PROTOCOL);
		
		started = false;
	}
	
	@Override
	public <T> void registerExecutor(Protocol protocol, Class<T> actionType, Class<? extends IExecutor<T>> executorType) {
		registerExecutorFactory(new CreateByTypeExecutorFactory<T>(protocol, actionType, executorType));
	}
	
	private class CreateByTypeExecutorFactory<T> implements IExecutorFactory<T> {
		private Protocol protocol;
		private Class<T> actionType;
		private Class<? extends IExecutor<T>> executorType;
		
		public CreateByTypeExecutorFactory(Protocol protocol, Class<T> actionType,
				Class<? extends IExecutor<T>> executorType) {
			this.protocol = protocol;
			this.actionType = actionType;
			this.executorType = executorType;
		}

		@SuppressWarnings("unchecked")
		@Override
		public IExecutor<T> create() {
			return (IExecutor<T>)createExecutor(executorType);
		}

		@Override
		public Protocol getProtocol() {
			return protocol;
		}

		@Override
		public Class<T> getActionType() {
			return actionType;
		}
		
	}
	
	@Override
	public <T> void registerExecutorFactory(IExecutorFactory<T> executorFactory) {
		if (executorFactories.containsKey(executorFactory.getActionType())) {
			throw new IllegalArgumentException(String.format("Reduplicate executor factory for action type: %s.",
					executorFactory.getActionType()));
		}
		
		oxmFactory.register(new IqProtocolChain(Execution.PROTOCOL).next(executorFactory.getProtocol()),
				new CocParserFactory<>(executorFactory.getActionType()));
		
		executorFactories.put(executorFactory.getActionType(), executorFactory);
	}
	
	@Override
	public boolean isExecutorRegistered(Class<?> actionType) {
		return executorFactories.containsKey(actionType);
	}
	
	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isStopped() {
		return !started;
	}
}
