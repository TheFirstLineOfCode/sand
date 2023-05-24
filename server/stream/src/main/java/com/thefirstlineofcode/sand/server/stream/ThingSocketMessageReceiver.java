package com.thefirstlineofcode.sand.server.stream;

import com.thefirstlineofcode.granite.framework.core.annotations.Component;
import com.thefirstlineofcode.granite.stream.standard.SocketMessageReceiver;

@Component("thing.socket.message.receiver")
public class ThingSocketMessageReceiver extends SocketMessageReceiver {
	@Override
	protected int getDefaultPort() {
		return 6222;
	}
}
