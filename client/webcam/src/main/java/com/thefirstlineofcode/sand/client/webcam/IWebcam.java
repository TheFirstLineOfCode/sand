package com.thefirstlineofcode.sand.client.webcam;

public interface IWebcam {
	public class Capability {
		public int width;
		public int height;
		public int maxFps;
		
		public Capability(int width, int height, int maxFps) {
			this.width = width;
			this.height = height;
			this.maxFps = maxFps;
		}
	}
	
	void start();
	void stop();
	boolean isStarted();
	boolean isStopped();
	void open();
	void close();
	boolean isOpened();
	boolean isClosed();
	void setRequestedCapability(Capability requestedCapability);
	Capability getRequestedCapability();
	Capability getOpenedCapability();
}
