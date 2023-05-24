package com.thefirstlineofcode.sand.client.lora.dac;

import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.thing.commuication.IAddressConfigurator;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.thing.IAddress;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraDacService<OA extends IAddress> extends IAddressConfigurator<ICommunicator<OA, LoraAddress, byte[]>,
			LoraAddress, byte[]> {
	public static final byte DEFAULT_THING_COMMUNICATION_CHANNEL = 0x17;
	
	public interface Listener {
		void addressConfigured(String thingId, LoraAddress address);
	}
	
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
	void setGatewayUplinkAddress(LoraAddress gatewayUplinkAddress);
	LoraAddress getGatewayUplinkAddress();
	void setGatewayDownlinkAddress(LoraAddress gatewayDownlinkAddress);
	LoraAddress getGatewayDownlinkAddress();
	void setThingCommunicationChannel(byte thingCommunicationChannel);
	byte getThingCommunicationChannel();
}
