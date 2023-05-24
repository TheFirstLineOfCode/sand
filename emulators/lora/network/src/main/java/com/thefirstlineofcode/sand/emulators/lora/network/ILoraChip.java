package com.thefirstlineofcode.sand.emulators.lora.network;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationChip;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraChip extends ICommunicationChip<LoraAddress, byte[]> {
	public enum PowerType {
		HIGH_POWER,
		NORMAL
	}
	
	boolean isSlept();
	void sleep();
	void sleep(int millis);
	void wakeUp();
	
	PowerType getPowerType();
	LoraAddress getAddress();
}
