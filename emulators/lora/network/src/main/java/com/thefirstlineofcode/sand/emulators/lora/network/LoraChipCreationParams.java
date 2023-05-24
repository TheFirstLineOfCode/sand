package com.thefirstlineofcode.sand.emulators.lora.network;

import com.thefirstlineofcode.sand.client.thing.commuication.ParamsMap;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraChipCreationParams extends ParamsMap {
	public static final String PARAM_NAME_TYPE = "type";
	public static final String PARAM_NAME_ADDRESS = "address";	
	
	public LoraChipCreationParams(LoraChip.PowerType type) {
		this(type, null);
	}
	
	public LoraChipCreationParams(LoraAddress address) {
		this(null, address);
	}

	
	public LoraChipCreationParams(LoraChip.PowerType type, LoraAddress address) {
		if (type != null)
			addParams(PARAM_NAME_TYPE, type);
		
		if (address != null)
			addParams(PARAM_NAME_ADDRESS, address);
	}

	public void setType(LoraChip.PowerType type) {
		addParams(PARAM_NAME_TYPE, type);
	}
	
	public LoraChip.PowerType getType() {
		return getParam(PARAM_NAME_TYPE);
	}

}
