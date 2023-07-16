package com.thefirstlineofcode.sand.client.lora.dac;

import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.thing.commuication.IAddressConfigurator;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.lora.dac.Allocation;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraDacService extends IAddressConfigurator<ICommunicator<LoraAddress, LoraAddress, byte[]>,
			LoraAddress, byte[]> {
	public interface Listener {
		void addressConfigured(String thingId, String registrationCode, LoraAddress address);
	}
	
	public interface Allocator {
		Allocation allocate(ILoraDacService dacService);
	}
	
	void setUplinkChannelBegin(int uplinkChannelBegin);
	int getUplinkChannelBegin();
	void setUplinkChannelEnd(int uplinkChannelEnd);
	int getUplinkChannelEnd();
	void setUplinkAddress(byte[] uplinkAddress);
	byte[] getUplinkAddress();
	void setConcentrator(IConcentrator concentrator);
	IConcentrator getConcentrator();
	void start();
	boolean isStarted();
	void stop();
	boolean isStopped();
	void reset();
	void addListener(Listener listener);
	boolean removeListener(Listener listener);
	void setDacServiceAddress(LoraAddress dacServiceAddress);
	LoraAddress getDacServiceAddress();
	void setThingCommunicationChannel(byte thingCommunicationChannel);
	byte getThingCommunicationChannel();
	void setAddressAllocator(Allocator addressAllocator);
}
