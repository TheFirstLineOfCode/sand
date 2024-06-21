package com.thefirstlineofcode.sand.demo.app.android;

import android.content.Context;

import com.thefirstlineofcode.amber.protocol.AmberWatchModelDescriptor;
import com.thefirstlineofcode.chalk.android.StandardChatClient;
import com.thefirstlineofcode.sand.client.operator.OperatorPlugin;
import com.thefirstlineofcode.sand.client.remoting.IRemoting;
import com.thefirstlineofcode.sand.client.remoting.RemotingPlugin;
import com.thefirstlineofcode.sand.client.sensor.SensorPlugin;
import com.thefirstlineofcode.sand.client.thing.ThingPlugin;
import com.thefirstlineofcode.sand.client.webcam.WebcamPlugin;
import com.thefirstlineofcode.sand.demo.client.DemoPlugin;
import com.thefirstlineofcode.sand.demo.protocols.Lgsc01ModelDescriptor;
import com.thefirstlineofcode.sand.emulators.models.Lge01ModelDescriptor;
import com.thefirstlineofcode.sand.emulators.models.Sle01ModelDescriptor;

import java.util.Collection;
import java.util.Collections;

class ChatClientSingleton {
	private static StandardChatClient chatClient;

	static StandardChatClient get(Context context) {
		if (chatClient == null) {
			chatClient = new StandardChatClient(Toolkits.getStreamConfig(context));
			chatClient.register(ThingPlugin.class);
			chatClient.register(OperatorPlugin.class);
			chatClient.register(RemotingPlugin.class);
			chatClient.register(SensorPlugin.class);
			chatClient.register(WebcamPlugin.class);
			chatClient.register(DemoPlugin.class);
			
			IRemoting remoting = chatClient.createApi(IRemoting.class);
			Collection<Class<?>> actionTypes = new Lge01ModelDescriptor().getSupportedActions().values();
			remoting.registerActions(Collections.list(Collections.enumeration(actionTypes)));
			
			actionTypes = new Sle01ModelDescriptor().getSupportedActions().values();
			remoting.registerActions(Collections.list(Collections.enumeration(actionTypes)));
			
			actionTypes = new Lgsc01ModelDescriptor().getSupportedActions().values();
			remoting.registerActions(Collections.list(Collections.enumeration(actionTypes)));
			
			actionTypes = new AmberWatchModelDescriptor().getSupportedActions().values();
			Collection<Class<?>> actionResultTypes = new AmberWatchModelDescriptor().getSupportedActionResults().values();
			remoting.registerActions(Collections.list(Collections.enumeration(actionTypes)),
					Collections.list(Collections.enumeration(actionResultTypes)));
		}

		return chatClient;
	}
	
	static void destroy() {
		if (chatClient == null)
			return;

		if (chatClient.isConnected())
			chatClient.close();

		chatClient = null;
	}
}
