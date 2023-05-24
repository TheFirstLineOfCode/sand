package com.thefirstlineofcode.sand.client.concentrator;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.basalt.xmpp.core.LangText;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;

public class ErrorCodeToXmppErrorsConverter implements ILanExecutionErrorConverter {
	private String model;
	private Map<Integer, Class<? extends IError>> errorNumberToXmppErrorTypes;
	
	public ErrorCodeToXmppErrorsConverter(String model, Map<Integer, Class<? extends IError>> errorNumberToXmppErrorTypes) {
		this.model = model;
		this.errorNumberToXmppErrorTypes = errorNumberToXmppErrorTypes;
	}
	
	@Override
	public String getModel() {
		return model;
	}
	
	@Override
	public IError convertErrorNumberToError(Integer errorNumber) {
		Class<?> errorType = errorNumberToXmppErrorTypes.get(errorNumber);
		
		try {
			IError error = (IError)errorType.getDeclaredConstructor().newInstance();
			error.setText(new LangText(ThingsUtils.getExecutionErrorDescription(model, errorNumber)));
			
			return error;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected error.", e);
		}
	}
}
