package com.thefirstlineofcode.sand.emulators.commons;

import java.util.Map;

import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.binary.BxmppConversionException;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationListener;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.client.thing.obx.ObxFactory;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.actuator.LanExecution;
import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanAnswer;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ResetThing;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public abstract class AbstractCommunicationNetworkThingEmulator<OA extends ILanAddress, PA extends ILanAddress> extends
		AbstractThingEmulator implements ICommunicationNetworkThingEmulator<OA, PA, byte[]>,
		ICommunicationListener<OA, PA, byte[]> {
	protected ICommunicator<OA, PA, byte[]> communicator;
	protected Map<Protocol, Class<?>> supportedActions;
	
	public AbstractCommunicationNetworkThingEmulator(String model) {
		super(model);
		
		this.communicator = createCommunicator();
		communicator.initialize();
		
		supportedActions = createSupportedActions();
	}
	
	@Override
	public void startToReceiveData() {
		if (communicator.isListening())
			return;
		
		communicator.addCommunicationListener(this);
		communicator.startToListen();
	}
	
	@Override
	public void stopDataReceving() {
		if (!communicator.isListening())
			return;
		
		communicator.stopToListen();
		communicator.removeCommunicationListener(this);		
	}
	
	protected abstract ICommunicator<OA, PA, byte[]> createCommunicator();
	
	@Override
	public void reset() {
		doReset();		
		getPanel().updateStatus(getThingStatus());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!getClass().equals(obj.getClass()))
			return false;
		
		return thingId.equals(((ICommunicationNetworkThingEmulator<?, ?, ?>)obj).getThingId());
	}
	
	@Override
	public ICommunicator<OA, PA, byte[]> getCommunicator() {
		return communicator;
	}
	
	@Override
	public void sent(PA to, byte[] data) {}
	
	@Override
	public void received(PA from, byte[] data) {
		processReceived(from, data);
	}
	
	protected void processReceived(PA from, byte[] data) {
		Protocol protocol = readProtocol(data);
		if (protocol == null) {
			throw new RuntimeException(String.format("Unrecognized protocol. Data: %s.", getDataInfoString(data)));
		}
		
		if (LanExecution.PROTOCOL.equals(protocol)) {
			processLanExecution(from, data);
		} else {
			processAction(from, protocol, data);
		}
	}

	protected void processAction(PA from, Protocol protocol, byte[] data)  {
		Class<?> actionType = supportedActions.get(protocol);
		Object action = readAction(actionType, data);
		
		if (actionType == null) {
			throw new RuntimeException(String.format("Action not supported. Protocol is %s.", protocol));
		}
		
		try {
			processAction(action);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	protected void processLanExecution(PA from, byte[] data) {
		LanExecution request = null;
		try {
			request = (LanExecution)ObxFactory.getInstance().toObject(data);
			Object action = request.getAction();
			
			processAction(action);
			sendToPeer(from, new LanAnswer(ThingsTinyId.createResponseId(request.getTraceId())));
		} catch (BxmppConversionException e) {
			throw new RuntimeException("Failed to convert BXMPP data to LAN execution object.", e);
		} catch (ExecutionException e) {
			sendToPeer(from, new LanAnswer(ThingsTinyId.createErrorId(request.getTraceId()), e.getErrorNumber()));
		}		
	}
	
	protected void sendToPeer(PA to, Object obj) {
		try {
			communicator.send(to, ObxFactory.getInstance().toBinary(obj));
		} catch (CommunicationException ce) {
			ce.printStackTrace();
		}
	}
	
	@Override
	public void occurred(CommunicationException e) {}
	
	protected Protocol readProtocol(byte[] data) {
		return ObxFactory.getInstance().readProtocol(data);
	}
	
	protected <A> A readAction(Class<A> actionType, byte[] data) {
		try {
			return (A)ObxFactory.getInstance().toObject(actionType, data);
		} catch (BxmppConversionException e) {
			throw new RuntimeException("Failed to convert BXMPP data to action object.", e);
		}
	}
	
	protected String getDataInfoString(byte[] data) {
		return BinaryUtils.getHexStringFromBytes(data);
	}
	
	protected void processAction(Object action) throws ExecutionException {
		if (action instanceof ResetThing) {
			reset();
		} else {			
			throw new RuntimeException(String.format("Unsupported action type: %s.", action.getClass().getName()));	
		}
	}
	
	protected abstract Map<Protocol, Class<?>> createSupportedActions();
}
