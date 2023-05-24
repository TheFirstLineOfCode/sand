package com.thefirstlineofcode.sand.demo.app.android;

import android.app.Application;

import com.thefirstlineofcode.chalk.android.logger.LogConfigurator;

import org.webrtc.PeerConnection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainApplication extends Application {
	public static final List<PeerConnection.IceServer> ICE_SERVERS = createIceServers();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		File dataDir = getApplicationContext().getFilesDir();
		new LogConfigurator().configure(dataDir.getAbsolutePath(), "sand-demo", LogConfigurator.LogLevel.INFO);
	}
	
	private static List<PeerConnection.IceServer> createIceServers() {
		List<PeerConnection.IceServer> iceServers = new ArrayList<>();
		PeerConnection.IceServer stunServer1 = PeerConnection.IceServer.builder(
					"stun:stunserver.stunprotocol.org:3478").createIceServer();
		iceServers.add(stunServer1);
		PeerConnection.IceServer stunServer2 = PeerConnection.IceServer.builder(
					"stun:120.78.128.21:3478").createIceServer();
		iceServers.add(stunServer2);
		
		PeerConnection.IceServer turnServer = PeerConnection.IceServer.builder(
				"turn:120.78.128.21:3478").
				setUsername("webrtc").
				setPassword("18814358626").
				createIceServer();
		iceServers.add(turnServer);
		
		return iceServers;
	}
}
