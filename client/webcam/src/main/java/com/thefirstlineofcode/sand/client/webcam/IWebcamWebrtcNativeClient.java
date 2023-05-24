package com.thefirstlineofcode.sand.client.webcam;

public interface IWebcamWebrtcNativeClient {
	public interface Listener {
		void processNativeMessage(String id, String data);
	}
	
	void setNativeServicePath(String nativeServicePath);
	String getNativeServicePath();
	void startNativeService();
	void stopNativeService();
	void setListener(Listener listener);
	void removeListener();
	void connect();
	boolean isConnected();
	void send(String message);
}
