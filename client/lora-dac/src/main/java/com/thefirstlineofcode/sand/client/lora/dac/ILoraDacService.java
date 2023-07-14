package com.thefirstlineofcode.sand.client.lora.dac;

import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.thing.commuication.IAddressConfigurator;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.thing.IAddress;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraDacService<OA extends IAddress> extends IAddressConfigurator<ICommunicator<OA, LoraAddress, byte[]>,
			LoraAddress, byte[]> {
	public interface Listener {
		void addressConfigured(String thingId, String registrationCode, LoraAddress address);
	}
	
	void setUplinkChannelBegin(int uplinkChannelBegin);
	void setUplinkChannelEnd(int uplinkChannelEnd);
	void setUplinkAddress(byte uplinkAddressHighByte, byte uplinkAddressLowByte);
	void setConcentrator(IConcentrator concentrator);
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
}
