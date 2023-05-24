package com.thefirstlineofcode.sand.demo.client;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;

public class AclError {
	public enum Type {
		SERVER_RETURNS_AN_ERROR,
		INVALID_UPDATE_REQUEST
	}
	
	private Type type;
	private StanzaError detail;
	
	public AclError(Type type) {
		this(type, null);
	}
	
	public AclError(Type type, StanzaError detail) {
		this.type = type;
		this.detail = detail;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public StanzaError getDetail() {
		return detail;
	}

	public void setDetail(StanzaError detail) {
		this.detail = detail;
	}
}
