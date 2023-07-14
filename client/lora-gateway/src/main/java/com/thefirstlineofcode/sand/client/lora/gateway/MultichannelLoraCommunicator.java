package com.thefirstlineofcode.sand.client.lora.gateway;

import java.util.List;

import com.thefirstlineofcode.sand.client.thing.commuication.AbstractCommunicator;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class MultichannelLoraCommunicator extends AbstractCommunicator<LoraAddress, LoraAddress, byte[]> implements ICommunicator<LoraAddress, LoraAddress, byte[]> {
	ICommunicator<LoraAddress, LoraAddress, byte[]> downlinkCommunicator;
	List<ICommunicator<LoraAddress, LoraAddress, byte[]>> uplinkCommunicators;
	
	public MultichannelLoraCommunicator(ICommunicator<LoraAddress, LoraAddress, byte[]> downlinkCommunicator,
			List<ICommunicator<LoraAddress, LoraAddress, byte[]>> uplinkCommunicators) {
		if (downlinkCommunicator == null)
			throw new IllegalStateException("Null downlink communicator.");
		
		if (uplinkCommunicators == null || uplinkCommunicators.size() == 0)
			throw new IllegalStateException("Null or size of uplink communicators is zero.");
		
		this.downlinkCommunicator = downlinkCommunicator;
		this.uplinkCommunicators = uplinkCommunicators;
	}
	
	@Override
	public LoraAddress getAddress() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void doInitialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doConfigure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doChangeAddress(LoraAddress address, boolean savePersistently) throws CommunicationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doStartToListen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doSend(LoraAddress to, byte[] data) throws CommunicationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopToListen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isListening() {
		// TODO Auto-generated method stub
		return false;
	}
}
