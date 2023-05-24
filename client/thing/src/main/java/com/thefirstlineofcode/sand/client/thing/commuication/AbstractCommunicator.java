package com.thefirstlineofcode.sand.client.thing.commuication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.chalk.core.IOrder;
import com.thefirstlineofcode.sand.protocols.thing.IAddress;

public abstract class AbstractCommunicator<OA extends IAddress, PA extends IAddress, D> implements ICommunicator<OA, PA, D> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractCommunicator.class);
	
	protected List<ICommunicationListener<OA, PA, D>> listeners;
	protected boolean initialized;

	public AbstractCommunicator() {
		listeners = new ArrayList<>();
		initialized = false;
	}
	
	@Override
	public void initialize() {
		if (initialized)
			return;
		
		doInitialize();
		
		initialized = true;
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}
	
	@Override
	public void configure() {
		if (!initialized)
			throw new RuntimeException("Not initialized. Call initialize method first.");
		
		doConfigure();
	}

	@Override
	public void changeAddress(OA address, boolean savePersistently) throws CommunicationException {
		if (!initialized)
			throw new CommunicationException("Not initialized. Call initialize method first.");
		
		try {
			doChangeAddress(address, savePersistently);
			if (logger.isInfoEnabled())
				logger.info(String.format("Address changes to '%s' %s.", address.toAddressString(),
						savePersistently ? "persistently" : "temporarily"));
		} catch (CommunicationException e) {
			if (logger.isErrorEnabled())
				logger.error("Failed to change address for communicator.", e);
			
			synchronized (this) {
				for (ICommunicationListener<OA, PA, D> listener : listeners) {
					listener.occurred(e);
				}
			}
			
			throw e;
		}
	}
	
	@Override
	public void send(PA to, D data) throws CommunicationException {
		if (!initialized)
			throw new CommunicationException("Not initialized. Call initialize method first.");
		
		try {
			doSend(to, data);
		} catch (CommunicationException e) {
			if (logger.isErrorEnabled())
				logger.error("Failed to send data to '{}'.", to.toAddressString());
			
			for (ICommunicationListener<OA, PA, D> listener : listeners) {
				listener.occurred(e);
			}
			
			throw e;
		}
		
		for (ICommunicationListener<OA, PA, D> listener : listeners) {
			listener.sent(to, data);
		}
	}
	
	@Override
	public void addCommunicationListener(ICommunicationListener<OA, PA, D> listener) {
		if (listeners.contains(listener))
			return;
		
		listeners.add(listener);
		Collections.sort(listeners, new OrderComparator<>());
	}
	
	@Override
	public void removeCommunicationListener(ICommunicationListener<OA, PA, D> listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void startToListen() {
		if (!initialized)
			throw new RuntimeException("Not initialized. Call initialize method first.");
		
		doStartToListen();
	}
	
	@Override
	public void received(final PA from, final D data) {
		for (ICommunicationListener<OA, PA, D> listener : listeners) {
			listener.received(from, data);
		}
	}
	
	protected abstract void doInitialize();
	protected abstract void doConfigure();
	protected abstract void doChangeAddress(OA address, boolean savePersistently) throws CommunicationException;
	protected abstract void doStartToListen();
	protected abstract void doSend(PA to, D data) throws CommunicationException;

	private class OrderComparator<T> implements Comparator<T> {
		@Override
		public int compare(T obj1, T obj2) {
			int orderOfObj1 = IOrder.ORDER_NORMAL;
			int orderOfObj2 = IOrder.ORDER_NORMAL;

			if (obj1 instanceof IOrder) {
				IOrder order1 = (IOrder) obj1;
				orderOfObj1 = order1.getOrder();
			}

			if (obj2 instanceof IOrder){
				IOrder order2 = (IOrder) obj2;
				orderOfObj2 = order2.getOrder();
			}

			return orderOfObj2 - orderOfObj1;
		}
	}
}
