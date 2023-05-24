package com.thefirstlineofcode.sand.demo.things.lgsc01;

import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.logger.LogConfigurator;
import com.thefirstlineofcode.chalk.logger.LogConfigurator.LogLevel;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.thing.ThingIdentity;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class Main {
	private LoraGatewayAndCamera gatewayAndCamera;
	
	public static void main(String[] args) {
		new Main().run(args);
	}

	private void run(String[] args) {
		if (args.length == 1 && args[0].equals("--help")) {
			printUsage();
			
			return;
		}
		
		String host = null;
		Integer port = null;
		boolean tlsPreferred = false;
		String logLevel = null;
		boolean disableCamera = false;
		boolean disableLoraGateway = false;
		boolean dontRunWebrtcNativeService = false;
		String webrtcNativeServicePath = null;
		String requestedWebcamCapability = null;
		boolean startConsole = false;
		
		for (int i = 0; i < args.length; i++) {
			if (!args[i].startsWith("--")) {
				throw new IllegalArgumentException("Illegal option format.");
			}
			
			int equalSignIndex = args[i].indexOf('=');
			if (equalSignIndex == 2 ||
					equalSignIndex == (args[i].length() - 1)) {
				throw new IllegalArgumentException("Illegal option format.");
			}
			
			String name, value;
			if (equalSignIndex == -1) {
				name = args[i].substring(2,  args[i].length());
				value = "TRUE";
			} else {
				name = args[i].substring(2, equalSignIndex).trim();
				value = args[i].substring(equalSignIndex + 1, args[i].length()).trim();
			}
			
			if ("help".equals(name)) {
				throw new IllegalArgumentException("Illegal option format.");
			} else if ("host".equals(name)) {
				host = value;
			} else if ("port".equals(name)) {
				port = Integer.parseInt(value);
			} else if ("tls-preferred".equals(name)) {
				tlsPreferred = Boolean.parseBoolean(value);
			} else if ("dont-run-webrtc-native-service".equals(name)) {
				dontRunWebrtcNativeService = Boolean.parseBoolean(value);
			} else if ("webrtc-native-service-path".equals(name)) {
				webrtcNativeServicePath = value;
			} else if ("log-level".equals(name)) {
				logLevel = value;
			} else if ("disable-camera".equals(name)) {				
				disableCamera = Boolean.parseBoolean(value);
			} else if ("disable-lora-gateway".equals(name)) {				
				disableLoraGateway = Boolean.parseBoolean(value);
			} else if ("requested-webcam-capability".equals(name)) {
				requestedWebcamCapability = value;
			} else if ("console".equals(name)) {
				startConsole = Boolean.parseBoolean(value);
			} else {
				throw new IllegalArgumentException(String.format("Unknown option: %s.", name));				
			}
		}
		
		if (logLevel == null)
			logLevel = "info";
		
		new LogConfigurator().configure(LoraGatewayAndCamera.THING_MODEL, getLogLevel(logLevel));
		
		WebcamConfig webcamConfig = new WebcamConfig(dontRunWebrtcNativeService, webrtcNativeServicePath, requestedWebcamCapability);
		ICommunicator<LoraAddress, LoraAddress, byte[]> communicator = null;
		if (!disableLoraGateway)
			communicator = new As32Ttl100LoraCommunicator();
		
		if (host != null) {
			if (port == null) {
				port = 6222;
			}
			
			StandardStreamConfig streamConfig = new StandardStreamConfig(host, port);
			streamConfig.setTlsPreferred(tlsPreferred);
			streamConfig.setResource(ThingIdentity.DEFAULT_RESOURCE_NAME);
			
			gatewayAndCamera = new LoraGatewayAndCamera(webcamConfig, streamConfig, communicator, disableCamera, disableLoraGateway, startConsole);
		} else {
			gatewayAndCamera = new LoraGatewayAndCamera(webcamConfig, communicator, disableCamera, disableLoraGateway, startConsole);
		}
		
		gatewayAndCamera.start();
	}
	
	private LogLevel getLogLevel(String sLogLevel) {
		if ("info".equals(sLogLevel))
			return LogLevel.INFO;
		else if ("debug".equals(sLogLevel))
			return LogLevel.DEBUG;
		else if ("trace".equals(sLogLevel)) {
			return LogLevel.TRACE;
		} else {
			throw new IllegalArgumentException(String.format("Illegal log level: %s.", sLogLevel));
		}
	}

	private void printUsage() {
		System.out.println("Usage: java sand-demo-things-sc-rbp3b--${VERSION}.jar [OPTIONS]");
		System.out.println("OPTIONS:");
		System.out.println("--help                            Display help information.");
		System.out.println("--host=HOST                       Specify host name of server.");
		System.out.println("--port=PORT                       Specify server port.");
		System.out.println("--tls-preferred                   Specify whether TLS is preferred when connecting to server.");
		System.out.println("--log-level=LOG_LEVEL             Specify log level. Option values are info, debug or trace.");
		System.out.println("--disable-camera                  Don't start camera service.");
		System.out.println("--dont-run-webrtc-native-service  Don't run WebRTC native service process.");
		System.out.println("--webrtc-native-service-path      Specify WebRTC native service path.");
		System.out.println("--requested-webcam-capability     Specify requested video capture capability to webcam. Capability format: WIDTH,HEIGHT,MAX_FPS.");
	}
}
