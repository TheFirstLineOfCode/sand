package com.thefirstlineofcode.sand.emulators.lora.network;

import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicatorFactory;
import com.thefirstlineofcode.sand.client.thing.commuication.ParamsMap;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraCommunicatorFactory implements ICommunicatorFactory {
	private static volatile LoraCommunicatorFactory instance;
	private ILoraNetwork network;
	
	private LoraCommunicatorFactory(ILoraNetwork network) {
		this.network = network;
	}

	@Override
	public ICommunicator<?, ?, ?> createCommunicator(ParamsMap params) {
		return new LoraCommunicator(network.createChip(getChipAddress(params)));
	}
	
	public LoraCommunicator createLoraCommunicator(ParamsMap params) {
		return (LoraCommunicator)createCommunicator(params);
	}

	private LoraAddress getChipAddress(ParamsMap params) {
		return (LoraAddress)params.getParam(LoraChipCreationParams.PARAM_NAME_ADDRESS);
	}
	
	public static synchronized void create(ILoraNetwork network) {
		if (instance != null)
			throw new IllegalStateException("Don't invoke create method two times.");
		
		instance = new LoraCommunicatorFactory(network);
	}
	
	public static LoraCommunicatorFactory getInstance() {
		if (instance == null)
			throw new IllegalStateException("Please invoke create method to reate instance first.");
		
		return instance;
	}
}
