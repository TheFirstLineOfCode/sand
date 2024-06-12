package com.thefirstlineofcode.sand.protocols.thing;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.thefirstlineofcode.sand.protocols.thing.lora.BleAddress;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public enum CommunicationNet {
	LORA,
	BLE;
	
	private static ConcurrentMap<String, Class<? extends ILanAddress>> types = new ConcurrentHashMap<>();
	
	static {
		types.put(LORA.name(), LoraAddress.class);
		types.put(BLE.name(), BleAddress.class);
	}
	
	public ILanAddress parse(String addressString) throws BadAddressException {
		try {
			Method parseMethod = getAddressType().getMethod("parse", new Class<?>[] {String.class});
			return (ILanAddress)parseMethod.invoke(null, addressString);
		} catch (Exception e) {
			throw new RuntimeException("Can't call static parse method of address.", e);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends ILanAddress> getAddressType() {
		Class<? extends ILanAddress> type = types.get(name());
		if (type != null)
			return type;
		
		String name = name();
		String className = String.format("%s.%s.%s%s%s",
				"com.thefirstlineofcode.sand.protocols",
				name.toLowerCase(),
				name.substring(0, 1).toUpperCase(),
				name.substring(1, name.length()).toLowerCase(),
				"Address"
		);
		
		try {
			type = (Class<? extends ILanAddress>)Class.forName(className);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't get address type. Address name is %s and Address class name is %s.",
					name(), className), e);
		}
		
		Class<? extends ILanAddress> existed = types.putIfAbsent(name, type);
		if (existed != null)
			type = existed;
		
		return type;
	}
}
