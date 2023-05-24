package com.thefirstlineofcode.sand.emulators.lora.network;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationNetwork;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraNetwork extends ICommunicationNetwork<LoraAddress, byte[], LoraChipCreationParams> {
	public enum SignalQuality {
		GOOD(5),
		MEDUIM(15),
		BAD(30),
		BADDEST(70);
		
		private int packetLossRate;
		
		private SignalQuality(int packetLossRate) {
			this.packetLossRate = packetLossRate;
		}
		
		public int getPacketLossRate() {
			return packetLossRate;
		}
	}
	
	ILoraChip createChip(LoraAddress address);
	ILoraChip createChip(LoraAddress address, LoraChipCreationParams params);
	void removeChip(LoraAddress address);
	void setSignalCrashedInterval(int interval);
	int getSignalCrashedInterval();
}
