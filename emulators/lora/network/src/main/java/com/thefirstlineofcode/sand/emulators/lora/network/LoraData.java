package com.thefirstlineofcode.sand.emulators.lora.network;

import com.thefirstlineofcode.sand.client.thing.commuication.Data;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraData extends Data<LoraAddress, byte[]> {
	public LoraData(LoraAddress address, byte[] data) {
		super(address, data);
	}
}
