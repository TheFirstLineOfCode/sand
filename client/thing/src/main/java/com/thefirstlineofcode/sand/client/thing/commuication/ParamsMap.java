package com.thefirstlineofcode.sand.client.thing.commuication;

import java.util.HashMap;
import java.util.Map;

public class ParamsMap {
	private Map<String, Object> params = new HashMap<>();
	
	public ParamsMap addParams(String name, Object value) {
		if (params.containsKey(name))
			throw new IllegalArgumentException(String.format("Reduplicate parameter. Parameter name %s has ready existed."));
		
		if (value == null)
			throw new IllegalArgumentException("Null parameter value.");
		
		params.put(name, value);
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getParam(String name) {
		return (T)params.get(name);
	}
	
	public Map<String, Object> getParams() {
		return params;
	}
}
