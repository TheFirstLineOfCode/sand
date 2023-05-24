package com.thefirstlineofcode.sand.client.lora.gateway;

import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacService;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.lora.gateway.WorkingMode;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraGateway {
	public enum Mode {
		WORKING,
		DAC
	}
	
	void setCommunicator(ICommunicator<LoraAddress, LoraAddress, byte[]> communicator);
	IConcentrator getConcentrator();
	ILoraDacService<LoraAddress> getDacService();
	void start();
	void stop();
	boolean isStarted();
	void setWorkingMode(WorkingMode workingMode);
	WorkingMode getWorkingMode();
}
