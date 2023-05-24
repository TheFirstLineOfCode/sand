package com.thefirstlineofcode.sand.client.webcam;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WebcamWebrtcNativeClient implements IWebcamWebrtcNativeClient {
	private static final int NATIVE_SERVICE_PORT = 9000;
	private static final int DEFAULT_BLOCKING_TIMEOUT = 128;
	
	private Listener listener;
	
	private Thread sendingThread;
	private Thread receivingThread;
	private Thread processingThread;
	
	private BlockingQueue<String> sendingQueue;
	private BlockingQueue<String> receivingQueue;
	
	private volatile boolean stopThreadsFlag;
	
	private Socket socket;
	
	private String nativeServicePath;
	private boolean connected;
		
	public WebcamWebrtcNativeClient(Listener listener) {
		this.listener = listener;
		
		sendingQueue = new ArrayBlockingQueue<>(16);
		receivingQueue = new ArrayBlockingQueue<>(16);
				
		nativeServicePath = getDefaultNativeServiceProgramPath();
		connected = false;
	}
	
	protected String getDefaultNativeServiceProgramPath() {
		if (isWindowsPlatform())
			return String.format("%s/%s/%s", System.getProperty("user.home"), "WebcamWebrtcNativeService", "WebcamWebrtcNativeService.exe");
		
		return String.format("%s/%s/%s", System.getProperty("user.home"), "WebcamWebrtcNativeService", "WebcamWebrtcNativeService");
	}

	private boolean isWindowsPlatform() {
		return System.getProperty("os.name").indexOf("Windows") != -1;
	}

	public void connect() {
		InetSocketAddress address = new InetSocketAddress("localhost", NATIVE_SERVICE_PORT);
		if (address.isUnresolved()) {
			throw new RuntimeException("Inet socket address is unresolved.");
		}
		
		try {
			if (socket == null) {
				socket = createSocket();
			}
			socket.connect(address, 4000);			
		} catch (IOException e) {
			processException("IO exception. Can't connect to native service.");
			return;
		}
		
		startThreads();
		connected = true;
	}
	
	private void processException(String message) {
		if (listener != null)
			listener.processNativeMessage("ERROR", message);
	}

	protected Socket createSocket() throws IOException {
		Socket socket = new Socket();
		socket.setSoTimeout(1000);
		socket.setTcpNoDelay(true);
		
		return socket;
	}
	
	private void startThreads() {
		stopThreadsFlag = false;
		
		sendingThread = new SendingThread();
		sendingThread.start();
		
		receivingThread = new ReceivingThread();
		receivingThread.start();
		
		processingThread = new ProcessingThread();
		processingThread.start();
	}
	
	private void stopThreads() {
		stopThreadsFlag = true;
		
		if (receivingThread != null) {
			try {
				receivingThread.join(DEFAULT_BLOCKING_TIMEOUT * 4, 0);
			} catch (InterruptedException e) {
				// ignore
			}
			receivingThread = null;
		}
		
		if (sendingThread != null) {
			try {
				sendingThread.join(DEFAULT_BLOCKING_TIMEOUT * 4, 0);
			} catch (InterruptedException e) {
				// ignore
			}
			sendingThread = null;
		}
		
		if (processingThread != null) {
			try {
				processingThread.join(DEFAULT_BLOCKING_TIMEOUT * 4, 0);
			} catch (InterruptedException e) {
				// ignore
			}
			processingThread = null;
		}
	}
	
	@Override
	public boolean isConnected() {
		return connected;
	}
	
	private class ReceivingThread extends Thread {
		private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
		private InputStream input;
		
		public ReceivingThread() {
			super("Webcam WEBRTC Native Service Message Receiving Thread");
		}
		
		@Override
		public void run() {
			try {
				input = new BufferedInputStream(socket.getInputStream());
			} catch (IOException e) {
				processException("Receiving thread can't be created");
				return;
			}
			
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			while (true) {
				try {
					if (stopThreadsFlag) {
						break;
					}
					
					int num = input.read(buf);	
					if (num == -1) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							throw new RuntimeException("Unexpected exception.", e);
						}
					} else {
						receivingQueue.put((new String(buf, 0, num)));
					}
				} catch (SocketTimeoutException e) {
					if (stopThreadsFlag)
						break;
				} catch (IOException e) {
					processException("Failed to read socket.");
					break;
				} catch (InterruptedException e) {
					processException("Failed to put message to receiving queue.");
				}
			}
		}
	}
	
	private void received(String message) {
		int lastChar = message.charAt(message.length() - 1);
		if (0 == lastChar)
			message = message.substring(0, message.length() - 1);
		
		int spaceCharIndex = message.indexOf(' ');
		if (spaceCharIndex != -1) {
			if (spaceCharIndex == (message.length() - 1)) {
				throw new RuntimeException("Illegal message format. Last char is blank space.");
			}
			
			String id = message.substring(0, spaceCharIndex);
			String data = message.substring(spaceCharIndex + 1, message.length());
			listener.processNativeMessage(id, data);
		} else {
			listener.processNativeMessage(message, null);
		}
	}
	
	@Override
	public void send(String message) {
		try {
			sendingQueue.put(message);
		} catch (InterruptedException e) {
			processException("Failed to put message to sending queue.");
		}
	}
	
	private class ProcessingThread extends Thread {
		public ProcessingThread() {
			super("Webcam WEBRTC Native Service Message Processing Thread");
		}
		
		public void run() {
			while (true) {
				try {
					String message = null;
					message = receivingQueue.poll(DEFAULT_BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS);
					
					if (stopThreadsFlag) {
						break;
					}
					
					if (message == null)
						continue;
					
					received(message);
				} catch (InterruptedException e) {
					break;
				}
				
			}
		}
	}
	
	private class SendingThread extends Thread {
		private OutputStream output;
		
		public SendingThread() {
			super("Webcam WEBRTC Native Service Message Sending Thread");
		}
		
		@Override
		public void run() {
			try {
				output = socket.getOutputStream();
			} catch (IOException e) {
				processException("Sending thread can't be created");
				return;
			}
			
			while (true) {
				try {
					String message = sendingQueue.poll(DEFAULT_BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS);
					
					if (stopThreadsFlag) {
						break;
					}
					
					if (message == null)
						continue;
					
					message += "$$";
					output.write(message.getBytes("UTF-8"));
					output.flush();
				} catch (InterruptedException e) {
					break;
				} catch (IOException e) {
					processException("Failed to write socket.");
					break;
				}
			}
		}
	}

	@Override
	public void setNativeServicePath(String nativeServicePath) {
		this.nativeServicePath = nativeServicePath;
	}

	@Override
	public String getNativeServicePath() {
		return nativeServicePath;
	}

	@Override
	public void startNativeService() {
		try {
			ProcessBuilder pb = new ProcessBuilder(nativeServicePath).
					redirectInput(Redirect.INHERIT).
					redirectError(Redirect.INHERIT).
					redirectOutput(Redirect.INHERIT);
			Map<String, String> env = pb.environment();
			for (String key : System.getenv().keySet()) {
				env.put(key, System.getenv(key));
			}
			
			pb.start();
		} catch (IOException e) {
			throw new RuntimeException("Can't run runtime process.", e);
		}
	}

	@Override
	public void stopNativeService() {
		if (socket != null && socket.isConnected()) {
			send("STOP");
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			stopThreads();
			
			if (socket != null && socket.isConnected()) {
				try {
					socket.close();
				} catch (IOException e) {}
			}
			
			connected = false;
		}
	}
	
	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	@Override
	public void removeListener() {
		listener = null;
	}
}
