package com.thefirstlineofcode.sand.emulators.commons;

import java.io.Serializable;

public class StreamConfigInfo implements Serializable {
	private static final long serialVersionUID = -7299697127995429977L;
	
	public String host;
	public int port;
	public boolean tlsPreferred;
	
	public StreamConfigInfo(String host, int port, boolean tlsPreferred) {
		this.host = host;
		this.port = port;
		this.tlsPreferred = tlsPreferred;
	}
}
