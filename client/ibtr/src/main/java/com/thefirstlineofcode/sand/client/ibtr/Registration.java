package com.thefirstlineofcode.sand.client.ibtr;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAuthorized;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.RemoteServerTimeout;
import com.thefirstlineofcode.chalk.core.AuthFailureException;
import com.thefirstlineofcode.chalk.core.ErrorException;
import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.core.ISyncTask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.chalk.core.stream.INegotiationListener;
import com.thefirstlineofcode.chalk.core.stream.IStream;
import com.thefirstlineofcode.chalk.core.stream.IStreamNegotiant;
import com.thefirstlineofcode.chalk.core.stream.NegotiationException;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.network.ConnectionException;
import com.thefirstlineofcode.chalk.network.ConnectionListenerAdapter;
import com.thefirstlineofcode.chalk.network.IConnectionListener;
import com.thefirstlineofcode.sand.protocols.ibtr.ThingRegister;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.protocols.thing.UnregisteredThing;

public class Registration extends ConnectionListenerAdapter implements IRegistration, INegotiationListener {
	private StandardStreamConfig streamConfig;
	private List<IConnectionListener> connectionListeners = new ArrayList<>();
	private List<INegotiationListener> negotiationListeners = new ArrayList<>();
	
	private boolean dontThrowConnectionException = false;
	
	@Override
	public RegisteredThing register(String thingId, String registrationKey) throws RegistrationException {
		IChatClient chatClient = new IbtrChatClient(streamConfig);
		chatClient.register(InternalIbtrPlugin.class);
		
		chatClient.addNegotiationListener(this);
		chatClient.addConnectionListener(this);
		
		try {
			chatClient.connect(null);
		} catch (ConnectionException e) {
			if (!chatClient.isClosed())
				chatClient.close();
			
			throw new RegistrationException(IbtrError.CONNECTION_ERROR, e);
		} catch (AuthFailureException e) {
			// it's impossible
		}
		
		try {
			return chatClient.getChatServices().getTaskService().execute(new RegisterTask(thingId, registrationKey));
		} catch (ErrorException e) {
			IError error = e.getError();
			if (error.getDefinedCondition().equals(RemoteServerTimeout.DEFINED_CONDITION)) {
				throw new RegistrationException(IbtrError.TIMEOUT);
			} else if (error.getDefinedCondition().equals(NotAcceptable.DEFINED_CONDITION)) {
				throw new RegistrationException(IbtrError.NOT_ACCEPTABLE);
			} else if (error.getDefinedCondition().equals(NotAuthorized.DEFINED_CONDITION)) {
				throw new RegistrationException(IbtrError.NOT_AUTHORIZED);
			} else if (error.getDefinedCondition().equals(Conflict.DEFINED_CONDITION)) {
				throw new RegistrationException(IbtrError.CONFLICT);
			} else {
				throw new RegistrationException(IbtrError.UNKNOWN, e);
			}
		} finally {
			if (!chatClient.isClosed()) {
				// To avoid throw ConnectionException.CONNECTION_CLOSED.
				dontThrowConnectionException = true;
				chatClient.close();
			}
		}
	}
	
	private class RegisterTask implements ISyncTask<Iq, RegisteredThing> {
		private ThingRegister thingRegister;
		
		public RegisterTask(String thingId, String registrationKey) {
			thingRegister = new ThingRegister();
			thingRegister.setRegister(new UnregisteredThing(thingId, registrationKey));
		}

		@Override
		public void trigger(IUnidirectionalStream<Iq> stream) {
			Iq iq = new Iq(Iq.Type.SET, thingRegister, Stanza.generateId("ibtr"));
			iq.setObject(thingRegister);
			
			stream.send(iq);
		}

		@Override
		public RegisteredThing processResult(Iq iq) {
			ThingRegister register = iq.getObject();
			return (RegisteredThing)register.getRegister();
		}
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Feature not implemented.");
	}

	@Override
	public void addConnectionListener(IConnectionListener listener) {
		connectionListeners.add(listener);
	}

	@Override
	public void removeConnectionListener(IConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	@Override
	public void addNegotiationListener(INegotiationListener listener) {
		negotiationListeners.add(listener);
	}

	@Override
	public void removeNegotiationListener(INegotiationListener listener) {
		negotiationListeners.remove(listener);
	}

	@Override
	public void before(IStreamNegotiant source) {
		for (INegotiationListener negotiationListener : negotiationListeners) {
			negotiationListener.before(source);
		}
	}

	@Override
	public void after(IStreamNegotiant source) {
		for (INegotiationListener negotiationListener : negotiationListeners) {
			negotiationListener.after(source);
		}
	}

	@Override
	public void occurred(NegotiationException exception) {
		for (INegotiationListener negotiationListener : negotiationListeners) {
			negotiationListener.occurred(exception);
		}
	}

	@Override
	public void done(IStream stream) {
		for (INegotiationListener negotiationListener : negotiationListeners) {
			negotiationListener.done(stream);
		}
	}

	@Override
	public void exceptionOccurred(ConnectionException exception) {
		if (dontThrowConnectionException)
			return;
		
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.exceptionOccurred(exception);
		}
	}

	@Override
	public void messageReceived(String message) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.messageReceived(message);
		}
	}

	@Override
	public void messageSent(String message) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.messageSent(message);
		}
	}

}
