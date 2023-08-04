package com.thefirstlineofcode.sand.client.concentrator;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.FeatureNotImplemented;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Forbidden;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.InternalServerError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAllowed;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAuthorized;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.PaymentRequired;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Redirect;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.RegistrationRequired;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.RemoteServerTimeout;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;

public class StandardErrorCodeMapping {
	public static final int ERROR_CODE_REDIRECT = 302;
	public static final int ERROR_CODE_BAD_REQUEST = 400;
	public static final int ERROR_CODE_NOT_AUTHORIZED = 401;
	public static final int ERROR_CODE_PAYMENT_REQUIRED = 402;
	public static final int ERROR_CODE_FORBIDDEN = 403;
	public static final int ERROR_CODE_ITEM_NOT_FOUND = 404;
	public static final int ERROR_CODE_NOT_ALLOWED = 405;
	public static final int ERROR_CODE_NOT_ACCEPTABLE = 406;
	public static final int ERROR_CODE_REGISTRATION_REQUIRED = 407;
	public static final int ERROR_CODE_CONFLICT = 409;
	public static final int ERROR_CODE_INTERNAL_SERVER_ERROR = 500;
	public static final int ERROR_CODE_FEATURE_NOT_IMPLEMENTED = 501;
	public static final int ERROR_CODE_SERVICE_UNAVAILABLE = 503;
	public static final int ERROR_CODE_REMOTE_SERVER_TIMEOUT = 504;
	
	private static Map<Integer, Class<? extends StanzaError>> errorCodeToErrorClass = new HashMap<>();
	
	static {
		errorCodeToErrorClass.put(ERROR_CODE_REDIRECT, Redirect.class);
		errorCodeToErrorClass.put(ERROR_CODE_BAD_REQUEST, BadRequest.class);
		errorCodeToErrorClass.put(ERROR_CODE_NOT_AUTHORIZED, NotAuthorized.class);
		errorCodeToErrorClass.put(ERROR_CODE_PAYMENT_REQUIRED, PaymentRequired.class);
		errorCodeToErrorClass.put(ERROR_CODE_FORBIDDEN, Forbidden.class);
		errorCodeToErrorClass.put(ERROR_CODE_ITEM_NOT_FOUND, ItemNotFound.class);
		errorCodeToErrorClass.put(ERROR_CODE_NOT_ALLOWED, NotAllowed.class);
		errorCodeToErrorClass.put(ERROR_CODE_NOT_ACCEPTABLE, NotAcceptable.class);
		errorCodeToErrorClass.put(ERROR_CODE_REGISTRATION_REQUIRED, RegistrationRequired.class);
		errorCodeToErrorClass.put(ERROR_CODE_CONFLICT, Conflict.class);
		errorCodeToErrorClass.put(ERROR_CODE_INTERNAL_SERVER_ERROR, InternalServerError.class);
		errorCodeToErrorClass.put(ERROR_CODE_FEATURE_NOT_IMPLEMENTED, FeatureNotImplemented.class);
		errorCodeToErrorClass.put(ERROR_CODE_SERVICE_UNAVAILABLE, ServiceUnavailable.class);
		errorCodeToErrorClass.put(ERROR_CODE_REMOTE_SERVER_TIMEOUT, RemoteServerTimeout.class);
	}
	
	public StanzaError codeToError(int errorCode) {
		Class<? extends StanzaError> errorClass = errorCodeToErrorClass.get(errorCode);
		if (errorClass != null) {
			try {
				Constructor<? extends StanzaError> constructor =  errorClass.getConstructor();
				return constructor.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Failed to instaniate the error.", e);
			}	
		}
		
		return null;
	}
}
