package com.thefirstlineofcode.sand.demo.things.lgsc01;

import com.thefirstlineofcode.sand.client.webcam.IWebcam;

public class WebcamConfig {
	public boolean dontRunWebrtcNativeService;
	public String webrtcNativeServicePath;
	public IWebcam.Capability requestedCapability;
	
	public WebcamConfig(boolean dontRunWebrtcNativeService, String webrtcNativeServicePath) {
		this(dontRunWebrtcNativeService, webrtcNativeServicePath, null);
	}
	
	public WebcamConfig(boolean dontRunWebrtcNativeService, String webrtcNativeServicePath,
			String requestedCapability) {
		this.dontRunWebrtcNativeService = dontRunWebrtcNativeService;
		this.webrtcNativeServicePath = webrtcNativeServicePath;
		this.requestedCapability = LoraGatewayAndCamera.getRequestedWebcamCapability(requestedCapability);
	}
}
