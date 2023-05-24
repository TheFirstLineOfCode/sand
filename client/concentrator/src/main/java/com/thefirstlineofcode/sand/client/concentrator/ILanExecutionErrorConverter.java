package com.thefirstlineofcode.sand.client.concentrator;

import com.thefirstlineofcode.basalt.xmpp.core.IError;

public interface ILanExecutionErrorConverter {
	String getModel();
	IError convertErrorNumberToError(Integer errorNumber);
}
