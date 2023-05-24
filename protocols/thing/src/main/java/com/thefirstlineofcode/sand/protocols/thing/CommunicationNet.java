package com.thefirstlineofcode.sand.protocols.thing;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public enum CommunicationNet {
	LORA;
	
	private static ConcurrentMap<String, Class<? extends IAddress>> types = new ConcurrentHashMap<>();
	
	static {
		types.put(LORA.name(), LoraAddress.class);
	}
	
	public IAddress parse(String addressString) throws BadAddressException {
		try {
			Method parseMethod = getAddressType().getMethod("parse", new Class<?>[] {String.class});
			return (IAddress)parseMethod.invoke(null, addressString);
		} catch (Exception e) {
			throw new RuntimeException("Can't call static parse method of address.", e);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends IAddress> getAddressType() {
		Class<? extends IAddress> type = types.get(name());
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
			type = (Class<? extends IAddress>)Class.forName(className);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't get address type. Address name is %s and Address class name is %s.",
					name(), className), e);
		}
		
		Class<? extends IAddress> existed = types.putIfAbsent(name, type);
		if (existed != null)
			type = existed;
		
		return type;
	}
}
