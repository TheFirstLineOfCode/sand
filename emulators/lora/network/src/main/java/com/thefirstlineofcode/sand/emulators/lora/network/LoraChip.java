package com.thefirstlineofcode.sand.emulators.lora.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationListener;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraChip implements ILoraChip {
	private static final Logger logger = LoggerFactory.getLogger(LoraChip.class);
	
	protected ILoraNetwork network;
	protected PowerType powerType;
	protected LoraAddress address;
	protected volatile boolean slept;
	
	protected List<ICommunicationListener<LoraAddress, LoraAddress, byte[]>> listeners;
	
	public LoraChip(ILoraNetwork network, PowerType powerType, LoraAddress address) {
		if (network == null)
			throw new IllegalArgumentException("Null network.");
		
		if (powerType == null)
			throw new IllegalArgumentException("Null lora chip power type.");
		
		if (address == null)
			throw new IllegalArgumentException("Null address.");
		
		this.network = network;
		this.powerType = powerType;
		this.address = address;
		
		listeners = new ArrayList<>();
	}
	
	@Override
	public PowerType getPowerType() {
		return powerType;
	}
	
	@Override
	public LoraAddress getAddress() {
		return address;
	}

	@Override
	public void send(LoraAddress to, byte[] message) {
		network.sendData(this, to, message);
	}
	
	@Override
	public void addListener(ICommunicationListener<LoraAddress, LoraAddress, byte[]> listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public boolean removeListener(ICommunicationListener<LoraAddress, LoraAddress, byte[]> listener) {
		return listeners.remove(listener);
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash += 31 * network.hashCode();
		hash += 31 * powerType.hashCode();
		hash += 31 * address.hashCode();
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LoraChip) {
			LoraChip other = (LoraChip)obj;
			return network.equals(other.network) && address.equals(other.address);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("LoraChip[%s, %s, %s]", network, powerType, address);
	}

	@Override
	public LoraData receive() {
		LoraData data = (LoraData)network.receiveData(this);
		
		if (data != null) {			
			for (ICommunicationListener<LoraAddress, LoraAddress, byte[]> listener : listeners) {
				listener.received(data.getAddress(), data.getData());
			}
		}
		
		return data;
	}

	@Override
	public void changeAddress(LoraAddress address) {
		LoraAddress oldAddress = this.address;
		
		if (oldAddress.equals(address)) {
			if (logger.isWarnEnabled()) {
				logger.warn("Trying to change to the same address.");
			}
			
			return;
		}
		
		network.changeAddress(this, address);
		this.address = address;
		
		for (ICommunicationListener<LoraAddress, LoraAddress, byte[]> listener : listeners) {
			listener.addressChanged(address, oldAddress);
		}
	}
	
	@Override
	public void sleep() {
		slept = true;
	}
	
	@Override
	public void sleep(int millis) {
		slept = true;
		
		new Timer().schedule(new TimerTask() {		
			@Override
			public void run() {
				wakeUp();
			}
		}, millis);
	}
	
	@Override
	public boolean isSlept() {
		return slept;
	}

	@Override
	public void wakeUp() {
		slept = false;
	}

}
