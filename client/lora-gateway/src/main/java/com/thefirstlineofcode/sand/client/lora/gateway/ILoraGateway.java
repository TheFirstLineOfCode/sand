package com.thefirstlineofcode.sand.client.lora.gateway;

import java.util.List;

import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacService;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.lora.gateway.WorkingMode;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraGateway {
	public static final byte DEFAULT_UPLINK_ADDRESS_HIGH_BYTE = 0x00;
	public static final byte DEFAULT_UPLINK_ADDRESS_LOW_BYTE = 0x00;
	public static final byte DEFAULT_THING_COMMUNICATION_CHANNEL = 0x17;
	
	public enum Mode {
		WORKING,
		DAC
	}
		
	int getChannels();
	void setDownlinkCommunicator(ICommunicator<LoraAddress, LoraAddress, byte[]> downlinkCommunicator);
	void setUplinkCommunicators(List<ICommunicator<LoraAddress, LoraAddress, byte[]>> uplinkCommunicators);
	void setThingCommunicationChannel(byte thingCommunicationChannel);
	IConcentrator getConcentrator();
	ILoraDacService<LoraAddress> getDacService();
	void start();
	void stop();
	boolean isStarted();
	void setWorkingMode(WorkingMode workingMode);
	WorkingMode getWorkingMode();
	LoraAddress[] getUplinkAddresses();
}
